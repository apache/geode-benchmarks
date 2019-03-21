#!/usr/bin/env bash

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
