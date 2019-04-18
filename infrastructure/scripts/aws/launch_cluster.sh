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

TAG=
COUNT=
CI=

while (( "$#" )); do
  case "$1" in
    -t|--tag )
      if [ "$2" ]; then
        TAG=$2
        shift
      else
        echo 'ERROR: "--tag" requires a non-empty argument.'
        exit 1
      fi
      ;;
    -c|--count )
      if [ "$2" ]; then
        COUNT=$2
        shift
      else
        echo 'ERROR: "--count" requires a non-empty argument.'
        exit 1
      fi
      ;;
    --ci )
      CI=1
      ;;
    -h|--help|-\? )
      echo "Usage: $(basename "$0") -t tag -c 4 [options ...] [-- arguments ...]"
      echo "Options:"
      echo "-t|--tag : Cluster tag"
      echo "-c|--count : The number of instances to start"
      echo "--ci : Set if starting instances for Continuous Integration"
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
      exit 1
      ;;
  esac
  shift
done

if [[ -z "${AWS_ACCESS_KEY_ID}" ]]; then
  export AWS_PROFILE="geode-benchmarks"
fi

if [ -z "${CI}" ]; then
  CI=0
fi

pushd ../../../
./gradlew launchCluster -Pci=${CI} --args "${TAG} ${COUNT}"
popd
