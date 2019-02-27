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
      echo "-m : Test metadata to output to file, comma-delimited (optional)"
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

OUTPUT=${OUTPUT:-output-${DATE}-${TAG}}
PREFIX="geode-performance-${TAG}"

if [[ -z "${AWS_ACCESS_KEY_ID}" ]]; then
  export AWS_PROFILE="geode-benchmarks"
fi

SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/.geode-benchmarks/${TAG}-privkey.pem"
HOSTS=`aws ec2 describe-instances --query 'Reservations[*].Instances[*].PrivateIpAddress' --filter "Name=tag:geode-benchmarks,Values=${TAG}" --output text`
HOSTS=$(echo ${HOSTS} | tr ' ' ',')
FIRST_INSTANCE=`aws ec2 describe-instances --query 'Reservations[*].Instances[*].PublicIpAddress' --filter "Name=tag:geode-benchmarks,Values=${TAG}" --output text | cut -f 1`

echo "FIRST_INSTANCE=${FIRST_INSTANCE}"
echo "HOSTS=${HOSTS}"

if [ -z "${VERSION}" ]; then
  if [ -z "${BRANCH}" ]; then
    echo "Specify --version or --branch."
    exit 1
  fi

  ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE "\
    rm -rf geode && \
    git clone ${REPO} && \
    cd geode && git checkout ${BRANCH}"

  set +e
  for i in {1..5}; do
    if ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE "\
      cd geode && \
      ./gradlew resolveDependencies"; then
      break
    fi
  done
  set -e

  ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE "\
    cd geode && \
    ./gradlew install installDist"


  VERSION=$(ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE geode/geode-assembly/build/install/apache-geode/bin/gfsh version)
fi

if [ -z "${VERSION}" ]; then
  echo "Either --version or --branch is required."
  exit 1
fi

if [ -z "${METADATA}" ]; then
  METADATA="'geode branch':'${BRANCH}','geode version':'${VERSION}','benchmark branch':'${BENCHMARK_BRANCH}'"
fi


ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE \
  rm -rf geode-benchmarks '&&' \
  git clone ${BENCHMARK_REPO} --branch ${BENCHMARK_BRANCH} '&&' \
  cd geode-benchmarks '&&' \
  ./gradlew -PgeodeVersion=${VERSION} benchmark -Phosts=${HOSTS} -Pmetadata="${METADATA}" "$@"

mkdir -p ${OUTPUT}

scp ${SSH_OPTIONS} -r geode@${FIRST_INSTANCE}:geode-benchmarks/geode-benchmarks/build/reports ${OUTPUT}/reports
BENCHMARK_DIRECTORY="$(ssh ${SSH_OPTIONS} geode@${FIRST_INSTANCE} ls -l geode-benchmarks/geode-benchmarks/build/ | grep benchmark | awk 'NF>1{print $NF}')"
scp ${SSH_OPTIONS} -r geode@${FIRST_INSTANCE}:geode-benchmarks/geode-benchmarks/build/${BENCHMARK_DIRECTORY} ${OUTPUT}
