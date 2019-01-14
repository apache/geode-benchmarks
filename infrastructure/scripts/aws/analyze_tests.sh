#!/usr/bin/env bash

OUTPUT_DIR=${1}

TOP_DIR=$(git rev-parse --show-toplevel)

echo ${TOP_DIR}

BASELINE_DIR="${OUTPUT_DIR}/baseline"
BRANCH_DIR="${OUTPUT_DIR}/branch"
BASELINE_BENCHMARKS="$(ls -td ${BASELINE_DIR}/benchmarks_* | tail -1)"
BRANCH_BENCHMARKS="$(ls -td ${BRANCH_DIR}/benchmarks_* | tail -1)"
pushd ${TOP_DIR}
./gradlew analyzeRun --args "${BASELINE_BENCHMARKS} ${BRANCH_BENCHMARKS}"
popd
