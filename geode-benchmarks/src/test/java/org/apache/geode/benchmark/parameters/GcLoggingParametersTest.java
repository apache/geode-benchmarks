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

package org.apache.geode.benchmark.parameters;

import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.SERVER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestConfig;

class GcLoggingParametersTest {

  private static final String JAVA_RUNTIME_VERSION = "java.runtime.version";

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
  public void withJava8() {
    System.setProperty(JAVA_RUNTIME_VERSION, "1.8.0_212-b03");
    final TestConfig testConfig = new TestConfig();
    GcLoggingParameters.configure(testConfig);
    assertThatJava8GcLog(testConfig);
  }

  @Test
  public void withJava11() {
    System.setProperty(JAVA_RUNTIME_VERSION, "11.0.4+11");
    final TestConfig testConfig = new TestConfig();
    GcLoggingParameters.configure(testConfig);
    assertThatJava9GcLog(testConfig);
  }

  @Test
  public void withJava12() {
    System.setProperty(JAVA_RUNTIME_VERSION, "12.0.2+10");
    final TestConfig testConfig = new TestConfig();
    GcLoggingParameters.configure(testConfig);
    assertThatJava9GcLog(testConfig);
  }

  @Test
  public void withJava13() {
    System.setProperty(JAVA_RUNTIME_VERSION, "13+33");
    final TestConfig testConfig = new TestConfig();
    GcLoggingParameters.configure(testConfig);
    assertThatJava9GcLog(testConfig);
  }

  private void assertThatJava8GcLog(TestConfig testConfig) {
    assertThat(testConfig.getJvmArgs().get(CLIENT)).contains("-Xloggc:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(SERVER)).contains("-Xloggc:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(LOCATOR)).contains("-Xloggc:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(CLIENT)).doesNotContain("-Xlog:gc:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(SERVER)).doesNotContain("-Xlog:gc:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(LOCATOR)).doesNotContain("-Xlog:gc:OUTPUT_DIR/gc.log");
  }

  private void assertThatJava9GcLog(TestConfig testConfig) {
    assertThat(testConfig.getJvmArgs().get(CLIENT)).contains("-Xlog:gc*:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(SERVER)).contains("-Xlog:gc*:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(LOCATOR)).contains("-Xlog:gc*:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(CLIENT)).doesNotContain("-Xloggc:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(SERVER)).doesNotContain("-Xloggc:OUTPUT_DIR/gc.log");
    assertThat(testConfig.getJvmArgs().get(LOCATOR)).doesNotContain("-Xloggc:OUTPUT_DIR/gc.log");
  }

}
