#!/usr/bin/env bash

FLY=$(command -v fly)

CONCOURSE_PIPELINE=apache-develop-main
CONCOURSE_JOB=Benchmark
CONCOURSE_URL=https://concourse.apachegeode-ci.info
CONCOURSE_TARGET=$( ${FLY} targets | grep ${CONCOURSE_URL} | awk '{ print $1;}')
mapfile -t builds < <( ${FLY} -t ${CONCOURSE_TARGET} builds -j ${CONCOURSE_PIPELINE}/${CONCOURSE_JOB} | grep succeeded )

for build in ${!builds[@]}; do
#    echo "Got: ${builds[${build}]}"
    build_num=$(echo ${builds[${build}]} | awk '{ print $1;}')
    echo "Build number is ${build_num}."
    benchmark_url=$( ${FLY} -t ${CONCOURSE_TARGET} watch -b ${build_num} | grep benchmarks-${CONCOURSE_PIPELINE} | grep files\.apachegeode-ci\.info )
    echo "GOT: ${benchmark_url}"
done
