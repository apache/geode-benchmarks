#!/usr/bin/env bash

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

set -e -o pipefail

DEFAULT_BENCHMARK_REPO='https://github.com/apache/geode-benchmarks'
BENCHMARK_REPO=${DEFAULT_BENCHMARK_REPO}
DEFAULT_BENCHMARK_BRANCH='develop'
BENCHMARK_BRANCH=${DEFAULT_BENCHMARK_BRANCH}

DEFAULT_REPO='https://github.com/apache/geode'
REPO=${DEFAULT_REPO}
DEFAULT_BRANCH='develop'
BRANCH=${DEFAULT_BRANCH}

DEFAULT_BASELINE_REPO='https://github.com/apache/geode'
BASELINE_REPO=${DEFAULT_BASELINE_REPO}
DEFAULT_BASELINE_VERSION=1.8.0
BASELINE_VERSION=${DEFAULT_BASELINE_VERSION}

TAG=
METADATA=
OUTPUT=

while (( "$#" )); do
  case $1 in
    -t|--tag )
      if [ "$2" ]; then
        TAG=$2
        shift
      else
        echo 'ERROR: "--tag" requires a non-empty option argument.'
        exit 1
      fi
      ;;
    -p|--br|--benchmark-repo )
      if [ "$2" ]; then
        BENCHMARK_REPO=$2
        shift
      fi
      ;;
    -e|--bb|--benchmark-branch )
      if [ "$2" ]; then
        BENCHMARK_BRANCH=$2
        shift
      fi
      ;;
    -m|--metadata )
      if [ "$2" ]; then
        METADATA=$2
        shift
      fi
      ;;
    -o|--output )
      if [ "$2" ]; then
        OUTPUT=$2
        shift
      fi
      ;;
    -r|--gr|--repo|--geode-repo )
      if [ "$2" ]; then
        REPO=$2
        shift
      fi
      ;;
    -b|--gb|--branch|--geode-branch )
      if [ "$2" ]; then
        BRANCH=$2
        shift
      fi
      ;;
    -v|--version|--geode-version )
      if [ "$2" ]; then
        VERSION=$2
        shift
      fi
      ;;
    -R|--bgr|--baseline-repo|--baseline-geode-repo )
      if [ "$2" ]; then
        BASELINE_REPO=$2
        shift
      fi
      ;;
    -B|--bgb|--baseline-branch|--baseline-geode-branch )
      if [ "$2" ]; then
        BASELINE_BRANCH=$2
        shift
      fi
      ;;
    -V|--bv|--baseline-version|--baseline-geode-version )
      if [ "$2" ]; then
        BASELINE_VERSION=$2
        shift
      fi
      ;;
    -h|--help|-\? )
      echo "Usage: $(basename "$0") -t tag [options ...] [-- arguments ...]"
      echo "Options:"
      echo "-t|--tag : Cluster tag"
      echo "-p|--benchmark-repo : Benchmark repo (default: ${DEFAULT_BENCHMARK_REPO})"
      echo "-e|--benchmark-branch : Benchmark branch (default: ${DEFAULT_BENCHMARK_BRANCH})"
      echo "-o|--output : Output directory (defaults: ./output-<date>-<tag>)"
      echo "-v|--geode-version : Geode version"
      echo "-r|--geode-repo : Geode repo (default: ${DEFAULT_REPO})"
      echo "-b|--geode-branch : Geode branch (default: ${DEFAULT_BRANCH})"
      echo "-V|--baseline-geode-version : Geode baseline version (default: ${DEFAULT_BASELINE_VERSION})"
      echo "-R|--baseline-geode-repo : Geode baseline repo (default: ${DEFAULT_BASELINE_REPO})"
      echo "-B|--baseline-geode-branch : Geode baseline branch"
      echo "-m|--metadata : Test metadata to output to file, comma-delimited"
      echo "-- : All subsequent arguments are passed to the benchmark task as arguments."
      echo "-h|--help|-? : This help message"
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

DATE=$(date '+%m-%d-%Y-%H-%M-%S')

if [ -z "${TAG}" ]; then
  echo "--tag argument is required."
  exit 1
fi

OUTPUT=${OUTPUT:-output-${DATE}-${TAG}}

set -x
if ! [[ "$OUTPUT" = /* ]]; then
  OUTPUT="$(pwd)/${OUTPUT}"
fi

if [[ -z "${BASELINE_BRANCH}" ]]; then
  ./run_tests.sh \
      -t ${TAG} \
      -v ${BASELINE_VERSION} \
      -p ${BENCHMARK_REPO} \
      -e ${BENCHMARK_BRANCH} \
      -o ${OUTPUT}/baseline \
      -m "${METADATA}" \
      -- "$@"
else
  ./run_tests.sh \
      -t ${TAG} \
      -r ${BASELINE_REPO} \
      -b ${BASELINE_BRANCH} \
      -p ${BENCHMARK_REPO} \
      -e ${BENCHMARK_BRANCH} \
      -o ${OUTPUT}/baseline \
      -m "${METADATA}" \
      -- "$@"
fi

if [[ -z "${VERSION}" ]]; then
  ./run_tests.sh \
      -t ${TAG} \
      -r ${REPO} \
      -b ${BRANCH} \
      -p ${BENCHMARK_REPO} \
      -e ${BENCHMARK_BRANCH} \
      -o ${OUTPUT}/branch \
      -m "${METADATA}" \
      -- "$@"
else
  ./run_tests.sh \
      -t ${TAG} \
      -v ${VERSION} \
      -p ${BENCHMARK_REPO} \
      -e ${BENCHMARK_BRANCH} \
      -o ${OUTPUT}/branch \
      -m "${METADATA}" \
      -- "$@"
fi

set +x

./analyze_tests.sh ${OUTPUT}
