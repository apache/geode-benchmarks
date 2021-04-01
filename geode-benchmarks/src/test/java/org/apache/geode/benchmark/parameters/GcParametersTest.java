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

import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestConfig;

class GcParametersTest {
  private static final String WITH_GC = "benchmark.withGc";
  private static final String JAVA_RUNTIME_VERSION = "java.runtime.version";
  private static final String XX_USE_ZGC = "-XX:+UseZGC";
  private static final String XX_USE_G_1_GC = "-XX:+UseG1GC";
  private static final String XX_USE_CONC_MARK_SWEEP_GC = "-XX:+UseConcMarkSweepGC";

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
  public void withDefault() {
    System.clearProperty(WITH_GC);
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertCms(testConfig);
  }

  @Test
  public void withCms() {
    System.setProperty(WITH_GC, "CMS");
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertCms(testConfig);
  }

  @Test
  public void withG1() {
    System.setProperty(WITH_GC, "G1");
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertG1(testConfig);
  }

  @Test
  public void withZ() {
    System.setProperty(WITH_GC, "Z");
    System.setProperty(JAVA_RUNTIME_VERSION, "11.0.4+11");
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertZ(testConfig);
  }

  @Test
  public void withZinJava8() {
    System.setProperty(WITH_GC, "Z");
    System.setProperty(JAVA_RUNTIME_VERSION, "1.8.0_212-b03");
    final TestConfig testConfig = new TestConfig();
    assertThatThrownBy(() -> GcParameters.configure(testConfig))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private void assertCms(TestConfig testConfig) {
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).contains(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).contains(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).doesNotContain(XX_USE_G_1_GC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).doesNotContain(XX_USE_G_1_GC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).doesNotContain(XX_USE_G_1_GC);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).doesNotContain(XX_USE_ZGC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).doesNotContain(XX_USE_ZGC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).doesNotContain(XX_USE_ZGC);
  }

  private void assertG1(TestConfig testConfig) {
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains(XX_USE_G_1_GC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).contains(XX_USE_G_1_GC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).contains(XX_USE_G_1_GC);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name()))
        .doesNotContain(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name()))
        .doesNotContain(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name()))
        .doesNotContain(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).doesNotContain(XX_USE_ZGC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).doesNotContain(XX_USE_ZGC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).doesNotContain(XX_USE_ZGC);
  }

  private void assertZ(TestConfig testConfig) {
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains(XX_USE_ZGC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).contains(XX_USE_ZGC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).contains(XX_USE_ZGC);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name()))
        .doesNotContain(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name()))
        .doesNotContain(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name()))
        .doesNotContain(XX_USE_CONC_MARK_SWEEP_GC);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).doesNotContain(XX_USE_G_1_GC);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).doesNotContain(XX_USE_G_1_GC);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).doesNotContain(XX_USE_G_1_GC);
  }

}
