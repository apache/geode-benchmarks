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

set -e

TAG=${1}
BRANCH=${2:-develop}
PREFIX="geode-performance-${TAG}"
DATE=$(date '+%m-%d-%Y-%H-%M-%S')
OUTPUT=output-${DATE}

INSTANCES=$(gcloud compute instance-groups list-instances ${PREFIX} | grep "${TAG}" | awk '{print $1}')


HOSTS=$(echo ${INSTANCES} | tr ' ' ',')

FIRST_INSTANCE=$(echo ${INSTANCES} | awk '{print $1}' )

gcloud compute ssh geode@$FIRST_INSTANCE --command="\
  rm -rf geode-benchmarks && \
  git clone --depth=1 https://github.com/apache/geode --branch ${BRANCH} && \
  git clone https://github.com/apache/geode-benchmarks && \
  cd geode-benchmarks && \
  ./gradlew --include-build ../geode benchmark -Phosts=${HOSTS}"

mkdir -p ${OUTPUT}
gcloud compute scp --recurse geode@${FIRST_INSTANCE}:geode-benchmarks/geode-benchmarks/build/benchmarks ${OUTPUT}
