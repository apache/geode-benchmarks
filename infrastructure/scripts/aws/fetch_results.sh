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

FLY=$(command -v fly)

CONCOURSE_PIPELINE=apache-develop-main
CONCOURSE_JOB=Benchmark
CONCOURSE_URL=https://concourse.apachegeode-ci.info
CONCOURSE_TARGET=$( ${FLY} targets | grep ${CONCOURSE_URL} | awk '{ print $1;}')
RESULTS_CACHE_DIR=${HOME}/.geode-benchmarks/results_cache


mapfile -t builds < <( ${FLY} -t ${CONCOURSE_TARGET} builds -j ${CONCOURSE_PIPELINE}/${CONCOURSE_JOB} | grep succeeded )

mkdir -p ${RESULTS_CACHE_DIR}

for build in ${!builds[@]}; do
#    echo "Got: ${builds[${build}]}"
    build_num=$(echo ${builds[${build}]} | awk '{ print $1;}')
    echo "Build number is ${build_num}."
    benchmark_url=$( ${FLY} -t ${CONCOURSE_TARGET} watch -b ${build_num} | grep benchmarks-${CONCOURSE_PIPELINE} | grep files\.apachegeode-ci\.info | sed "s/^.*\(http.*tgz\).*/\1/")
    echo "GOT: ${benchmark_url}"
    benchmark_filename=$(basename ${benchmark_url})
    curl -o ${RESULTS_CACHE_DIR}/${benchmark_filename} ${benchmark_url}
done
