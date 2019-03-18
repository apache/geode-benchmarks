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

TAG=
METADATA=
OUTPUT=
VERSION=

while :; do
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
      echo "-m|--metadata : Test metadata to output to file, comma-delimited (optional)"
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
      break
      ;;
    * )
      break
  esac
  shift
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

fixRepoName() {
  if [ -z "$1" ]; then
    return 1
  elif [ ${1:0:5} = "https" ]; then
    echo ${1}
  else
    echo "https://github.com/${1}"
  fi
}

BENCHMARK_REPO=$(fixRepoName ${BENCHMARK_REPO})
REPO=$(fixRepoName ${REPO})

SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/.geode-benchmarks/${TAG}-privkey.pem"
HOSTS=`aws ec2 describe-instances --query 'Reservations[*].Instances[*].PrivateIpAddress' --filter "Name=tag:geode-benchmarks,Values=${TAG}" --output text`
HOSTS=$(echo ${HOSTS} | tr ' ' ',')
FIRST_INSTANCE=`aws ec2 describe-instances --query 'Reservations[*].Instances[*].PublicIpAddress' --filter "Name=tag:geode-benchmarks,Values=${TAG}" --output text | cut -f 1`

echo "FIRST_INSTANCE=${FIRST_INSTANCE}"
echo "HOSTS=${HOSTS}"

if [[ -z "${VERSION}" ]]; then
  if [[ -z "${BRANCH}" ]]; then
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

if [[ -z "${VERSION}" ]]; then
  echo "Either --version or --branch is required."
  exit 1
fi

set +e
ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE "
  [[ ! -r .geode-benchmarks-identifier ]] && \
  uuidgen > .geode-benchmarks-identifier"
set -e

instance_id=$(ssh ${SSH_OPTIONS} geode@$FIRST_INSTANCE cat .geode-benchmarks-identifier)


ssh ${SSH_OPTIONS} geode@${FIRST_INSTANCE} \
  rm -rf geode-benchmarks '&&' \
  git clone ${BENCHMARK_REPO} --branch ${BENCHMARK_BRANCH}

BENCHMARK_SHA=$(ssh ${SSH_OPTIONS} geode@${FIRST_INSTANCE} \
  cd geode-benchmarks '&&' \
  git rev-parse --verify -q HEAD)

BUILD_IDENTIFIER="$(uuidgen)"

METADATA="${METADATA},'source_repo':'${GEODE_REPO}','benchmark_repo':'${BENCHMARK_REPO}','benchmark_branch':'${BENCHMARK_BRANCH}','instance_id':'${instance_id}','benchmark_sha':'${BENCHMARK_SHA}','build_identifier':'${BUILD_IDENTIFIER}'"

ssh ${SSH_OPTIONS} geode@${FIRST_INSTANCE} \
  cd geode-benchmarks '&&' \
  ./gradlew -PgeodeVersion=${VERSION} benchmark -Phosts=${HOSTS} -Pmetadata="${METADATA}" "$@"

mkdir -p ${OUTPUT}

scp ${SSH_OPTIONS} -r geode@${FIRST_INSTANCE}:geode-benchmarks/geode-benchmarks/build/reports ${OUTPUT}/reports
BENCHMARK_DIRECTORY="$(ssh ${SSH_OPTIONS} geode@${FIRST_INSTANCE} ls -l geode-benchmarks/geode-benchmarks/build/ | grep benchmark | awk 'NF>1{print $NF}')"
scp ${SSH_OPTIONS} -r geode@${FIRST_INSTANCE}:geode-benchmarks/geode-benchmarks/build/${BENCHMARK_DIRECTORY} ${OUTPUT}
