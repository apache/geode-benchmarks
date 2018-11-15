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
FROM ubuntu
# This should be built from the top level of the repository.
# docker build -t geode-performance-testing-image -f images/geode-performance/Dockerfile
RUN apt update -y && \
  apt install -y openjdk-8-jdk-headless openssh-server
COPY . /geode-performance
RUN mkdir /var/run/sshd && \
  sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
  sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd && \
  ssh-keygen -N "" -f /root/.ssh/id_rsa && \
  echo "Host localhost" > /root/.ssh/config && \
  echo "    StrictHostKeyChecking no" >> /root/.ssh/config && \
  chmod 600 /root/.ssh/config && \
  cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys && \
  chmod 600 /root/.ssh/authorized_keys && \
  rm -f /geode-performance/Dockerfile
CMD ["/usr/sbin/sshd", "-D", "-E", "/var/log/sshd.log"]
EXPOSE 22
