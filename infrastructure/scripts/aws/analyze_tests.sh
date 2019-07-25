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

BASELINE_DIR=
BRANCH_DIR=
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
    --baseline|--baselineDir )
      if [ "$2" ]; then
        BASELINE_DIR=$2
        shift
      fi
      ;;
    --branch|--branchDir )
      if [ "$2" ]; then
        BRANCH_DIR=$2
        shift
      fi
      ;;
    --ci )
      CI=1
      ;;
    -h|--help|-\? )
      echo "Usage: $(basename "$0") [-o <output directory> | [--baselineDir <baseline directory> --branchDir <branch directory>]] [options ...] [-- arguments ...]"
      echo "Options:"
      echo "-o|--output|--outputDir : The directory containing benchmark results"
      echo "--baseline|--baselineDir : The directory containing baseline benchmark results"
      echo "--branch|--branchDir : The directory containing branch benchmark results"
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

if [[ ! -z "${OUTPUT_DIR}" ]]; then
  if [[ ! -z "${BRANCH_DIR}" ]] || [[ ! -z "${BASELINE_DIR}" ]]; then
    echo "Please specify either --outputDir or a combination of --baselineDir and --branchDir"
    exit 1
  fi
  BASELINE_DIR="${OUTPUT_DIR}/baseline"
  BRANCH_DIR="${OUTPUT_DIR}/branch"
fi

if [[ -z "${BRANCH_DIR}" ]] || [[ -z "${BASELINE_DIR}" ]]; then
  echo "Both --baselineDir and --branchDir should be specified"
  exit 1
fi

if [[ ${BASELINE_DIR} != /* ]]; then
    BASELINE_DIR="${PWD}/${BASELINE_DIR}"
fi

if [[ ${BRANCH_DIR} != /* ]]; then
    BRANCH_DIR="${PWD}/${BRANCH_DIR}"
fi

BASELINE_BENCHMARKS="$(ls -td ${BASELINE_DIR}/benchmarks_* | tail -1)"
BRANCH_BENCHMARKS="$(ls -td ${BRANCH_DIR}/benchmarks_* | tail -1)"
pushd ${TOP_DIR}
./gradlew analyzeRun -Pci=${CI} --args "${BASELINE_BENCHMARKS} ${BRANCH_BENCHMARKS}"
popd
