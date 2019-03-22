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

RESULTS_CACHE_DIR=${HOME}/.geode-benchmarks/results_cache

for i in $(ls ${RESULTS_CACHE_DIR}); do
  echo "Processing results archive: ${i}"
  ABSOLUTE_FILENAME=${RESULTS_CACHE_DIR}/${i}
  RESULTS_TEMP_DIR=$(mktemp -d -q -t "results-cache-${USER}")

  tar zxf ${ABSOLUTE_FILENAME} -C ${RESULTS_TEMP_DIR}

  RESULTS_BASE_DIR=$(ls -d ${RESULTS_TEMP_DIR}/*)
  INSTANCE_ID=$(basename ${RESULTS_BASE_DIR})
  for results_dir in $(ls ${RESULTS_BASE_DIR}); do
    full_results_dir=$(ls -d ${RESULTS_BASE_DIR}/${results_dir}/benchmarks_*)

    echo "Processing results for ${full_results_dir}"
    ./submit_benchmark.py  --benchmark_dir ${full_results_dir} --instance_id ${INSTANCE_ID}
  done
  rm -rf ${RESULTS_TEMP_DIR}
done
