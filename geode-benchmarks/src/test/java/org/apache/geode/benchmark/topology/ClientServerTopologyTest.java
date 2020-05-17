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


import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestConfig;

public class ClientServerTopologyTest {

  private Properties systemProperties;

  @BeforeEach
  public void beforeEach() {
    systemProperties = (Properties) System.getProperties().clone();
  }

  @AfterEach
  public void afterEach() {
    System.setProperties(systemProperties);
  }

  @Test
  public void configWithSsl() {
    System.setProperty("withSsl", "true");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-DwithSsl=true");
  }

  @Test
  public void configWithNoSsl() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).doesNotContain("-DwithSsl=true");
  }

  @Test
  public void configWithoutSecurityManager() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name()))
        .doesNotContain("-DwithSecurityManager=true");
  }

  @Test
  public void configWithSecurityManager() {
    System.setProperty("withSecurityManager", "true");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-DwithSecurityManager=true");
  }

  @Test
  public void configWithSecurityManagerAndSslAndJava11() {
    System.setProperty("withSecurityManager", "true");
    System.setProperty("java.runtime.version", "11.0.4+11");
    System.setProperty("withSsl", "true");
    TestConfig testConfig = new TestConfig();

    ClientServerTopology.configure(testConfig);

    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-DwithSecurityManager=true");
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-DwithSsl=true");
  }
}
