/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.perftest.infrastructure.ssh;

import java.util.Arrays;
import java.util.Collection;

import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.infrastructure.InfrastructureFactory;

public class SshInfrastructureFactory implements InfrastructureFactory {

  private final Collection<String> hosts;
  private final String user;

  public SshInfrastructureFactory(String user, String... hosts) {
    this.hosts = Arrays.asList(hosts);
    this.user = user;
  }

  @Override
  public Infrastructure create(int nodes) throws Exception {
    return new SshInfrastructure(hosts, user);
  }

  public Collection<String> getHosts() {
    return hosts;
  }

  public String getUser() {
    return user;
  }
}
