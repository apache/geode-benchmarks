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

DATE=$(date '+%m-%d-%Y-%H-%M-%S')
IMAGE_FAMILY="geode-performance"
INSTANCE_NAME="geode-performance-builder-${DATE}"
IMAGE_NAME="${IMAGE_FAMILY}-${DATE}"

echo "Launching an instance to build an image"
gcloud beta compute instances create "${INSTANCE_NAME}" --machine-type=n1-standard-8 --subnet=default --network-tier=PREMIUM --image-family=ubuntu-1804-lts --image-project=ubuntu-os-cloud --boot-disk-size=15GB --boot-disk-type=pd-ssd --boot-disk-device-name="${INSTANCE_NAME}"

echo "Installing docker, java, and geode dependencies on image"
gcloud compute ssh  "geode@${INSTANCE_NAME}" --command="\
set -e && \
sudo apt update && \
sudo apt install -y openjdk-8-jdk unzip dstat && \
sudo update-java-alternatives -s java-1.8.0-openjdk-amd64"  --  -o ConnectionAttempts=120

gcloud compute instances stop "${INSTANCE_NAME}"

echo "Creating an image from the instance"
gcloud compute images create ${IMAGE_NAME} --family=${IMAGE_FAMILY} --source-disk=${INSTANCE_NAME}

echo "Image created.  Stopping instance..."
gcloud compute instances delete "${INSTANCE_NAME}" --quiet

echo "All done. The new image is called ${IMAGE_NAME}"
