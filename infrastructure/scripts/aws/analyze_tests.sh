#!/usr/bin/env bash
set -e
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

TOP_DIR=$(git rev-parse --show-toplevel)

OUTPUT_DIR=
CI=0

echo ${TOP_DIR}

while (( "$#" )); do
  case "$1" in
    -o|--output|--outputDir )
      if [ "$2" ]; then
        OUTPUT_DIR=$2
        shift
      fi
      ;;
    --ci )
      CI=1
      ;;
    -h|--help|-\? )
      echo "Usage: $(basename "$0") -t tag -c 4 [options ...] [-- arguments ...]"
      echo "Options:"
      echo "-o|--output|--outputDir : The directory containing benchmark results"
      echo "--ci : Set if starting instances for Continuous Integration"
      echo "-- : All subsequent arguments are passed to the benchmark task as arguments."
      echo "-h|--help : This help message"
      exit 1
      ;;
    -- )
      shift
      break 2
      ;;
    -?* )
      printf 'Invalid option: %s\n' "$1" >&2
      exit 1
      ;;
  esac
  shift
done

BASELINE_DIR="${OUTPUT_DIR}/baseline"
BRANCH_DIR="${OUTPUT_DIR}/branch"
BASELINE_BENCHMARKS="$(ls -td ${BASELINE_DIR}/benchmarks_* | tail -1)"
BRANCH_BENCHMARKS="$(ls -td ${BRANCH_DIR}/benchmarks_* | tail -1)"
pushd ${TOP_DIR}
./gradlew analyzeRun -Pci=${CI} --args "${BASELINE_BENCHMARKS} ${BRANCH_BENCHMARKS}"
popd
