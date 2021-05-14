/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.benchmark.topology;


import static org.apache.geode.benchmark.Constants.JAVA_RUNTIME_VERSION;
import static org.apache.geode.benchmark.Constants.JAVA_VERSION_11;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Topology.WITH_SECURITY_MANAGER_PROPERTY;
import static org.apache.geode.benchmark.topology.Topology.WITH_SSL_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.perftest.TestConfig;

public class ClientServerTopologyTest {

  @Test
  @SetSystemProperty(key = WITH_SSL_PROPERTY, value = "true")
  public void configWithSsl() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-Dbenchmark.withSsl=true");
  }

  @Test
  @ClearSystemProperty(key = WITH_SSL_PROPERTY)
  public void configWithNoSsl() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name()))
        .doesNotContain("-Dbenchmark.withSsl=true");
  }

  @Test
  @ClearSystemProperty(key = WITH_SECURITY_MANAGER_PROPERTY)
  public void configWithoutSecurityManager() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name()))
        .doesNotContain("-Dbenchmark.withSecurityManager=true");
  }

  @Test
  @SetSystemProperty(key = WITH_SECURITY_MANAGER_PROPERTY, value = "true")
  public void configWithSecurityManager() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name()))
        .contains("-Dbenchmark.withSecurityManager=true");
  }

  @Test
  @SetSystemProperty(key = WITH_SECURITY_MANAGER_PROPERTY, value = "true")
  @SetSystemProperty(key = WITH_SSL_PROPERTY, value = "true")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_11)
  public void configWithSecurityManagerAndSslAndJava11() {
    TestConfig testConfig = new TestConfig();

    ClientServerTopology.configure(testConfig);

    assertThat(testConfig.getJvmArgs().get(CLIENT.name()))
        .contains("-Dbenchmark.withSecurityManager=true");
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-Dbenchmark.withSsl=true");
  }
}
