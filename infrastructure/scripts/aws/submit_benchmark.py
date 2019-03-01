#!/usr/bin/env python3

import argparse
import json
import pprint as pp
import psycopg2
import sys, os
import creds

parser = argparse.ArgumentParser()
parser.add_argument('benchmark_dir', help='Directory containing the benchmark to be submitted')
args = parser.parse_args()

with open(f"{args.benchmark_dir}/metadata.json", "r") as read_file:
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
conn_string = "host="+ creds.PGHOST +" port="+ "5432" +" dbname="+ creds.PGDATABASE +" user=" + creds.PGUSER \
              +" password="+ creds.PGPASSWORD
conn=psycopg2.connect(conn_string)
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

table_values = []
for junk in table_columns:
    table_values.append("%s")

sql_command = f"INSERT INTO public.benchmark_build({','.join(table_columns)}) values ({','.join(table_values)}) returning build_id"

print(f"SQL COMMAND IS: {sql_command}")

cursor.execute(sql_command, (geode_ci_sha, geode_benchmark_sha, geode_build_version, instance_id, benchmarks_raw_results_uri, notes, geode_build_sha))
build_id = cursor.fetchone()[0]
print("Completed command.")

conn.commit()

cursor.execute("select * from public.benchmark_build")

x = cursor.fetchall()

pp.pprint(x)
cursor.close()
conn.close()
