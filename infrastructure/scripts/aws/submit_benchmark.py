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
geode_ci_sha = ""
geode_benchmark_sha = ""
geode_build_version = ""
instance_id = ""
benchmarks_raw_results_uri = ""
notes = ""
geode_build_sha = ""

if data["instanceId"] is not None:
    instance_id = data["instanceId"]

if data["testMetadata"] is not None:
    testmetadata = data["testMetadata"]
    if testmetadata["geode version"] is not None:
        geode_build_version = testmetadata["geode version"]

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
table_columns = [
    "geode_ci_sha",
    "geode_benchmark_sha",
    "geode_build_version",
    "instance_id",
    "benchmarks_raw_results_uri",
    "notes",
    "geode_build_sha"
]

if build_identifier is not None:
    table_columns.append("build_identifier")

table_values = []
for junk in table_columns:
    table_values.append("%s")

sql_command = f"INSERT INTO public.benchmark_build({','.join(table_columns)}) " \
    f"values ({','.join(table_values)}) returning build_id"

if build_identifier is not None:
    sql_tuple = (geode_ci_sha, geode_benchmark_sha, geode_build_version, instance_id,
                 benchmarks_raw_results_uri, notes, geode_build_sha, build_identifier)
else:
    sql_tuple = (geode_ci_sha, geode_benchmark_sha, geode_build_version, instance_id,
             benchmarks_raw_results_uri, notes, geode_build_sha)

cursor.execute(sql_command, sql_tuple)
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
