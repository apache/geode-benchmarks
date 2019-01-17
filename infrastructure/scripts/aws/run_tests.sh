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

set -x -e -o pipefail

BENCHMARK_BRANCH='develop'

TEMP=`getopt t:b:v:m:o:h "$@"`
eval set -- "$TEMP"

while true ; do
    case "$1" in
        -t)
            TAG=$2 ; shift 2 ;;
        -m)
            BENCHMARK_BRANCH=$2 ; shift 2 ;;
        -o)
            OUTPUT=$2 ; shift 2 ;;
        -b)
            BRANCH=$2 ; shift 2 ;;
        -v)
            VERSION=$2 ; shift 2 ;;
        -h)
            echo "Usage: run_test.sh -t [tag] [-v [version] | -b [branch]] <options...>"
            echo "Options:"
            echo "-m : Benchmark branch (optional - defaults to develop)"
            echo "-o : Output directory (optional - defaults to ./output-<date>-<tag>"
            echo "-v : Geode Version"
            echo "-b : Geode Branch"
            echo "-t : Cluster tag"
            echo "-h : This help message"
            shift 2
            exit 1 ;;
        --) shift ; break ;;
        *) echo "Internal error!" ; exit 1 ;;
    esac
done


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

SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/.ssh/geode-benchmarks/${TAG}.pem"
HOSTS=`aws ec2 describe-instances --query 'Reservations[*].Instances[*].PrivateIpAddress' --filter "Name=tag:geode-benchmarks,Values=${TAG}" --output text`
HOSTS=$(echo ${HOSTS} | tr ' ' ',')
FIRST_INSTANCE=`aws ec2 describe-instances --query 'Reservations[*].Instances[*].PublicIpAddress' --filter "Name=tag:geode-benchmarks,Values=${TAG}" --output text | cut -f 1`

echo "FIRST_INSTANCE=${FIRST_INSTANCE}"
echo "HOSTS=${HOSTS}"

if [ ! -z "${BRANCH}" ]; then
  if [ ! -z "${VERSION}" ]; then
    echo "Specify --version or --branch."
    exit 1
  fi

  ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE "\
    rm -rf geode && \
    git clone https://github.com/apache/geode --branch ${BRANCH}"

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


ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE "\
  rm -rf geode-benchmarks && \
  git clone https://github.com/apache/geode-benchmarks --branch ${BENCHMARK_BRANCH} && \
  cd geode-benchmarks && \
  ./gradlew -PgeodeVersion=${VERSION} benchmark -Phosts=${HOSTS}"

mkdir -p ${OUTPUT}

scp ${SSH_OPTIONS} -r geode@${FIRST_INSTANCE}:geode-benchmarks/geode-benchmarks/build/reports ${OUTPUT}/reports
BENCHMARK_DIRECTORY="$(ssh ${SSH_OPTIONS} geode@${FIRST_INSTANCE} ls -l geode-benchmarks/geode-benchmarks/build/ | grep benchmark | awk 'NF>1{print $NF}')"
scp ${SSH_OPTIONS} -r geode@${FIRST_INSTANCE}:geode-benchmarks/geode-benchmarks/build/${BENCHMARK_DIRECTORY} ${OUTPUT}
