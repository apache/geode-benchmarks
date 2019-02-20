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

set -o pipefail

BENCHMARK_REPO='https://github.com/apache/geode-benchmarks'
BENCHMARK_BRANCH='develop'

REPO='https://github.com/apache/geode'

while getopts ":t:h" opt; do
  case ${opt} in
    t )
      TAG=$OPTARG
      ;;
    h )
      echo "Usage: $(basename "$0") -t tag -- command args ..."
      echo "Options:"
      echo "-t : Cluster tag"
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

if [ -z "${TAG}" ]; then
  echo "--tag argument is required."
  exit 1
fi

if [[ -z "${AWS_ACCESS_KEY_ID}" ]]; then
  export AWS_PROFILE="geode-benchmarks"
fi

SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/.ssh/geode-benchmarks/${TAG}.pem"
HOSTS=`aws ec2 describe-instances --query 'Reservations[*].Instances[*].PublicIpAddress' --filter "Name=tag:geode-benchmarks,Values=${TAG}" --output text`

for host in ${HOSTS}; do
  echo "Executing on ${host}."
  ssh ${SSH_OPTIONS} geode@${host} "$@"
done
