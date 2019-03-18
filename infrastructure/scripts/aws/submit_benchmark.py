#!/usr/bin/env python3

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import json
import psycopg2
import creds
import csv
import glob
import datetime
import pprint as pp

parser = argparse.ArgumentParser()
parser.add_argument('--benchmark_dir', '-b',
                    help='Directory containing the benchmark to be submitted')
parser.add_argument('--identifier', '-i', help='Unique identifier for this benchmark result')
args = parser.parse_args()

benchmark_dir = args.benchmark_dir.rstrip('/')
build_identifier = args.identifier

with open(f"{benchmark_dir}/metadata.json", "r") as read_file:
    data = json.load(read_file)


# what we need to create a benchmark_build entry
ci_sha = ""
benchmark_sha = ""
build_version = ""
instance_id = ""
benchmarks_raw_results_uri = ""
notes = ""
build_sha = ""

if data["testMetadata"] is not None:
    testmetadata = data["testMetadata"]
    if testmetadata["instance_id"] is not None:
        instance_id = testmetadata["instance_id"]
    if testmetadata["source_version"] is not None:
        build_version = testmetadata["source_version"]
    if testmetadata["source_revision"] is not None:
        build_sha = testmetadata["source_revision"]
    if testmetadata["benchmark_sha"] is not None:
        benchmark_sha = testmetadata["benchmark_sha"]
    if (build_identifier is None or build_identifier == "") and testmetadata["build_identifier"] is not None:
        build_identifier = testmetadata["build_identifier"]

# Set up a connection to the postgres server.
conn_string = "host=" + creds.PGHOST + \
              " port=5432" + \
              " dbname=" + creds.PGDATABASE + \
              " user=" + creds.PGUSER + \
              " password=" + creds.PGPASSWORD
conn = psycopg2.connect(conn_string)
print("Connected!")

# Create a cursor object
cursor = conn.cursor()

# figure out if we've already submitted the data
identifier_command = f"select build_id from public.benchmark_build where build_identifier = %s"
cursor.execute(identifier_command, (build_identifier,))
rows = cursor.fetchall()

if len(rows) > 0:
    print("This build data has already been submitted to the database.")
    exit(1)


table_columns = [
    "ci_sha",
    "benchmark_sha",
    "build_version",
    "instance_id",
    "benchmarks_raw_results_uri",
    "notes",
    "build_sha",
    "build_identifier"
]

table_values = []
for junk in table_columns:
    table_values.append("%s")

sql_command = f"INSERT INTO public.benchmark_build({','.join(table_columns)}) " \
    f"values ({','.join(table_values)}) returning build_id"

cursor.execute(sql_command, (ci_sha, benchmark_sha, build_version, instance_id, benchmarks_raw_results_uri, notes, build_sha, build_identifier))
build_id = cursor.fetchone()[0]
conn.commit()

if data["testNames"] is not None:
    testnames = data["testNames"]
    for testname in testnames:
        testdir = f"{benchmark_dir}/{testname}"
        clientdirs = glob.glob(f"{testdir}/client-*")
        for clientdir in clientdirs:
            latencyfilename = f"{clientdir}/latency_csv.hgrm"
            sql_command = f"INSERT INTO " \
                f"public.latency_result(build_id, benchmark_test, value, percentile, " \
                f"total_count, one_by_one_minus_percentile) values({build_id}, '{testname}'," \
                f" %s, %s, %s, %s)"
            print(f"Submitting latency data for {testname}")
            with open(latencyfilename) as f:
                reader = csv.DictReader(filter(lambda row: row[0] != '#', f))
                data = [r for r in reader]
                for datum in data:
                    if datum['1/(1-Percentile)'] != 'Infinity':
                        cursor.execute(sql_command, (datum['Value'],
                                                     datum['Percentile'],
                                                     datum['TotalCount'],
                                                     datum['1/(1-Percentile)']))
            # conn.commit()
            yardstickdirs = glob.glob(f"{clientdir}/*-yardstick-output")
            yardstickdir = yardstickdirs[0] if (yardstickdirs is not None) else None
            if yardstickdir is not None:
                throughputfilename = f"{yardstickdir}/ThroughputLatencyProbe.csv"
                sql_command = f"INSERT INTO public.throughput_result(build_id, benchmark_test, " \
                    f"timestamp, ops_per_sec) values({build_id}, '{testname}', %s, %s)"
                print(f"Submitting throughput data for {testname}")
                with open(throughputfilename) as f:
                    reader = csv.DictReader(filter(lambda row: row[0] != '#' and
                                                               row[0] != '-' and
                                                               row[0] != '@' and
                                                               row[0] != '*', f),
                                            fieldnames=('time', 'operations', 'latency'))
                    data = [r for r in reader]
                    for datum in data:
                        cursor.execute(sql_command,
                                       (datetime.datetime.fromtimestamp(int(datum['time'])),
                                        int(float(datum['operations']))))
            conn.commit()

cursor.close()
conn.close()
