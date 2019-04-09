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
import hashlib
import json
import os
import re

import psycopg2
import creds
import csv
import glob
import datetime

parser = argparse.ArgumentParser()
parser.add_argument('--benchmark_dir', '-b',
                    help='Directory containing the benchmark to be submitted')
parser.add_argument('--identifier', '-i', help='Unique identifier for this benchmark result')
parser.add_argument('--instance_id', '-I', help='instance id to use if not present in metadata')
args = parser.parse_args()
build_identifier = ""
benchmark_dir = args.benchmark_dir.rstrip('/')
print("***************************")
print(f"processing benchmark data set in {benchmark_dir}.")

if args.identifier is not None:
    build_identifier = args.identifier
metadata_file = f"{benchmark_dir}/metadata.json"
with open(metadata_file, "r") as read_file:
    metadata_string = read_file.read()
    data = json.loads(metadata_string)


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
    if 'instance_id' in testmetadata and testmetadata["instance_id"] is not None:
        instance_id = testmetadata["instance_id"]
    if (instance_id is None or instance_id == "") and args.instance_id is not None:
        instance_id = args.instance_id
    if 'source_version' in testmetadata and testmetadata["source_version"] is not None:
        build_version = testmetadata["source_version"]
    if 'source_revision' in testmetadata and testmetadata["source_revision"] is not None:
        build_sha = testmetadata["source_revision"]
    if 'benchmark_sha' in testmetadata and testmetadata["benchmark_sha"] is not None:
        benchmark_sha = testmetadata["benchmark_sha"]
    if (build_identifier is None or build_identifier == "") and \
            'build_identifier' in testmetadata and \
            testmetadata["build_identifier"] is not None:
        build_identifier = testmetadata["build_identifier"]

if build_identifier == "":
    m = hashlib.sha1()
    m.update(metadata_string.encode('utf-8'))
    m.update(f"{os.path.getmtime(metadata_file):.9f}".encode('utf-8'))
    build_identifier = m.hexdigest()

print(f"The build identifier for this benchmark dataset is {build_identifier} ")

if instance_id == "":
    possible_benchmark_archive_dir = benchmark_dir + "/../.."
    possible_instance_id = os.path.basename(os.path.abspath(possible_benchmark_archive_dir))
    if re.search(r'Benchmark-\d+-\d+',possible_instance_id) is not None:
        instance_id = possible_instance_id

print(f"The instance id for this benchmark dataset is {instance_id}")

# Set up a connection to the postgres server.
conn_string = "host=" + creds.PGHOST + \
              " port=5432" + \
              " dbname=" + creds.PGDATABASE + \
              " user=" + creds.PGUSER + \
              " password=" + creds.PGPASSWORD
conn = psycopg2.connect(conn_string)
print("Connected to database!")

# Create a cursor object
cursor = conn.cursor()

# figure out if we've already submitted the data
identifier_command = f"select build_id from public.benchmark_build where build_identifier = %s"
cursor.execute(identifier_command, (build_identifier,))
rows = cursor.fetchall()

if len(rows) > 0:
    print("* This build data has already been submitted to the database.")
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
