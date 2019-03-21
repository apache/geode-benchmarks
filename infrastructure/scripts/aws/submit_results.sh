#!/usr/bin/env bash

RESULTS_CACHE_DIR=${HOME}/.geode-benchmarks/results_cache

for i in $(ls ${RESULTS_CACHE_DIR}); do
  echo "Processing results archive: ${i}"
  ABSOLUTE_FILENAME=${RESULTS_CACHE_DIR}/${i}
  RESULTS_TEMP_DIR=$(mktemp -d -q -t "results-cache-${USER}")

  tar zxf ${ABSOLUTE_FILENAME} -C ${RESULTS_TEMP_DIR}

  RESULTS_BASE_DIR=$(ls -d ${RESULTS_TEMP_DIR}/*)

  for results_dir in $(ls ${RESULTS_BASE_DIR}); do
    full_results_dir=$(ls -d ${RESULTS_BASE_DIR}/${results_dir}/benchmarks_*)

    echo "Processing results for ${full_results_dir}"
    ./submit_benchmark.py  --benchmark_dir ${full_results_dir}
  done
  rm -rf ${RESULTS_TEMP_DIR}
done
