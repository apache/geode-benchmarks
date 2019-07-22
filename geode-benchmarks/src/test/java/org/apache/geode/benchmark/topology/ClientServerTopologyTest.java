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


import static org.apache.geode.benchmark.parameters.JVMParameters.JVM8_ARGS;
import static org.apache.geode.benchmark.parameters.JVMParameters.JVM_ARGS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestConfig;

public class ClientServerTopologyTest {


  @BeforeEach
  @AfterEach
  public void clearProperties() {
    System.clearProperty("withSsl");
    System.clearProperty("withSecurityManager");
  }

  @Test
  public void configWithSsl() {
    System.setProperty("withSsl", "true");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).contains("-DwithSsl=true");
  }

  @Test
  public void configWithNoSsl() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain("-DwithSsl=true");
  }

  @Test
  public void configWithJava8() {
    System.setProperty("java.runtime.version", "1.8.0_212");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).contains(JVM8_ARGS);
  }

  @Test
  public void configWithJava9() {
    System.setProperty("java.runtime.version", "9.0.1");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain(JVM8_ARGS);
  }

  @Test
  public void configWithoutSecurityManager() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain("-DwithSecurityManager=true");
  }

  @Test
  public void configWithSecurityManager() {
    System.setProperty("withSecurityManager", "true");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).contains("-DwithSecurityManager=true");
  }

  @Test
  public void configWithSecurityManagerAndSslAndJava9() {
    System.setProperty("withSecurityManager", "true");
    System.setProperty("java.runtime.version", "9.0.1");
    System.setProperty("withSsl", "true");
    TestConfig testConfig = new TestConfig();

    ClientServerTopology.configure(testConfig);

    assertThat(testConfig.getJvmArgs().get("client")).contains("-DwithSecurityManager=true");
    assertThat(testConfig.getJvmArgs().get("client")).contains("-DwithSsl=true");
    assertThat(testConfig.getJvmArgs().get("client")).contains(JVM_ARGS);
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain(JVM8_ARGS);
  }
}
