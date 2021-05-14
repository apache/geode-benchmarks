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
    -i|--instance-type )
      if [ "$2" ]; then
        INSTANCE_TYPE=$2
        shift
      else
        echo 'ERROR: "--instance-type" requires a non-empty argument.'
        exit 1
      fi
      ;;
    --tenancy )
      if [ "$2" ]; then
        TENANCY=$2
        shift
      else
        echo 'ERROR: "--tenancy" requires a non-empty argument.'
        exit 1
      fi
      ;;
    --availability-zone )
      if [ "$2" ]; then
        AVAILABILITY_ZONE=$2
        shift
      else
        echo 'ERROR: "--availability-zone" requires a non-empty argument.'
        exit 1
      fi
      ;;
    --ci )
      CI=1
      ;;
    -p|--purpose )
      if [ "${2}" ]; then
        PURPOSE=${2}
        shift
      else
        echo 'ERROR: "--purpose" requires a non-empty argument.'
        exit 1
      fi
      ;;
    -h|--help|-\? )
      echo "Usage: $(basename "$0") -t tag -c 4 [options ...] [-- arguments ...]"
      echo "Options:"
      echo "-t|--tag : Cluster tag"
      echo "-c|--count : The number of instances to start"
      echo "-i|--instance-type : The instance type to start"
      echo "--tenancy : Optionally 'host' or 'dedicated' (default)"
      echo "--availability-zone : Optionally AWS AZ, default 'us-west-2a'"
      echo "--ci : Set if starting instances for Continuous Integration"
      echo "-p|--purpose : Purpose (Purpose tag to use for base AMI)"
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

INSTANCE_TYPE=${INSTANCE_TYPE:-"c5.18xlarge"}
AVAILABILITY_ZONE=${AVAILABILITY_ZONE:-"us-west-2a"}
TENANCY=${TENANCY:-"host"}
CI=${CI:-0}
PURPOSE=${PURPOSE:-"geode-benchmarks"}

pushd ../../../
./gradlew launchCluster -Pci=${CI} -Ppurpose=${PURPOSE} \
      -PinstanceType=${INSTANCE_TYPE} \
      -Ptenancy=${TENANCY} \
      -PavailabilityZone=${AVAILABILITY_ZONE} \
      --args "${TAG} ${COUNT}"
popd
