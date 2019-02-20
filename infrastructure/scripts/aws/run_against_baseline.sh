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

while getopts ":t:r:b:v:p:e:R:B:V:m:o:h" opt; do
  case ${opt} in
    t )
      TAG=$OPTARG
      ;;
    p )
      BENCHMARK_REPO=$OPTARG
      ;;
    e )
      BENCHMARK_BRANCH=$OPTARG
      ;;
    m )
      METADATA=$OPTARG
      ;;
    o )
      OUTPUT=$OPTARG
      ;;
    r )
      REPO=$OPTARG
      ;;
    b )
      BRANCH=$OPTARG
      ;;
    v )
      VERSION=$OPTARG
      ;;
    R )
      BASELINE_REPO=$OPTARG
      ;;
    B )
      BASELINE_BRANCH=$OPTARG
      ;;
    V )
      BASELINE_VERSION=$OPTARG
      ;;
    h )
      echo "Usage: $(basename "$0") -t tag [options ...] [-- arguments ...]"
      echo "Options:"
      echo "-t : Cluster tag"
      echo "-p : Benchmark repo (default: ${DEFAULT_BENCHMARK_REPO})"
      echo "-e : Benchmark branch (default: ${DEFAULT_BENCHMARK_BRANCH})"
      echo "-o : Output directory (defaults: ./output-<date>-<tag>)"
      echo "-v : Geode version"
      echo "-r : Geode repo (default: ${DEFAULT_REPO})"
      echo "-b : Geode branch (default: ${DEFAULT_BRANCH})"
      echo "-V : Geode baseline version (default: ${DEFAULT_BASELINE_VERSION})"
      echo "-R : Geode baseline repo (default: ${DEFAULT_BASELINE_REPO})"
      echo "-B : Geode baseline branch"
      echo "-m : Test metadata to output to file, comma-delimited"
      echo "-- : All subsequent arguments are passed to the benchmark task as arguments."
      echo "-h : This help message"
      exit 1
      ;;
    \? )
      echo "Invalid option: $OPTARG" 1>&2
      ;;
    : )
      echo "Invalid option: $OPTARG requires an argument" 1>&2
      ;;
  esac
done
shift $((OPTIND -1))

DATE=$(date '+%m-%d-%Y-%H-%M-%S')

if [ -z "${TAG}" ]; then
  echo "--tag argument is required."
  exit 1
fi

if [ -z "${METADATA}" ]; then
  METADATA="'geode branch':'${BRANCH}','geode version':'${VERSION}','baseline branch':'${BASELINE_BRANCH}','baseline version':'${BASELINE_VERSION}','benchmark branch':'${BENCHMARK_BRANCH}'"
fi

OUTPUT=${OUTPUT:-output-${DATE}-${TAG}}

set -x
if ! [[ "$OUTPUT" = /* ]]; then
  OUTPUT="$(pwd)/${OUTPUT}"
fi

if [ -z "${VERSION}" ]; then
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

if [ -z "${BASELINE_VERSION}" ]; then
  ./run_tests.sh \
      -t ${TAG} \
      -r ${BASELINE_REPO} \
      -b ${BASELINE_BRANCH} \
      -p ${BENCHMARK_REPO} \
      -e ${BENCHMARK_BRANCH} \
      -o ${OUTPUT}/baseline \
      -m "${METADATA}" \
      -- "$@"
else
  ./run_tests.sh \
      -t ${TAG} \
      -v ${BASELINE_VERSION} \
      -p ${BENCHMARK_REPO} \
      -e ${BENCHMARK_BRANCH} \
      -o ${OUTPUT}/baseline \
      -m "${METADATA}" \
      -- "$@"
fi
set +x

./analyze_tests.sh ${OUTPUT}
