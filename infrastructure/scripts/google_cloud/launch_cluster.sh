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
COUNT=${2}
SUBNET=${3}
IMAGE_FAMILY="geode-performance"
PREFIX="geode-performance-${TAG}"
NODE_TEMPLATE="${PREFIX}-node-template"
NODE_GROUP="${PREFIX}-node-group"
INSTANCE_TYPE=n1-highmem-96

KEY_FILE=/tmp/id_${TAG}

ssh-keygen -b 2048 -t rsa -f $KEY_FILE -q -N ""

gcloud compute sole-tenancy node-templates create ${NODE_TEMPLATE} --node-type=n1-node-96-624
gcloud compute sole-tenancy node-groups create ${NODE_GROUP} --node-template=${NODE_TEMPLATE} --target-size=${COUNT}

gcloud compute instance-templates create ${PREFIX}-template \
  --machine-type=${INSTANCE_TYPE} \
  --subnet=${SUBNET}  \
  --node-group="${NODE_GROUP}" \
  --image-family=${IMAGE_FAMILY} \
  --boot-disk-size=50GB \
  --boot-disk-type=pd-ssd

gcloud compute instance-groups managed create ${PREFIX} --base-instance-name=${PREFIX} --template=${PREFIX}-template --size=${COUNT}

gcloud compute  instance-groups managed wait-until-stable ${PREFIX} --timeout=120

INSTANCES=$(gcloud compute instance-groups list-instances ${PREFIX} | grep "${TAG}" | awk '{print $1}')
for instance in ${INSTANCES}; do
  echo -n "Setting up ${instance}..."
  gcloud compute ssh geode@${instance} --command="echo ." --  -o ConnectionAttempts=120
  gcloud compute scp ${KEY_FILE} geode@${instance}:/home/geode/.ssh/id_rsa
  gcloud compute scp ${KEY_FILE}.pub geode@${instance}:/home/geode/.ssh/id_rsa.pub
  gcloud compute ssh geode@${instance} --command="cat /home/geode/.ssh/id_rsa.pub >> /home/geode/.ssh/authorized_keys"
done

rm ${KEY_FILE}
rm ${KEY_FILE}.pub

echo "$INSTANCES"
