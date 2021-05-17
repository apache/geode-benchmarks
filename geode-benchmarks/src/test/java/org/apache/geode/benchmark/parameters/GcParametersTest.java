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

import static org.apache.geode.benchmark.Constants.JAVA_RUNTIME_VERSION;
import static org.apache.geode.benchmark.Constants.JAVA_VERSION_11;
import static org.apache.geode.benchmark.Constants.JAVA_VERSION_8;
import static org.apache.geode.benchmark.topology.RoleKinds.GEODE_PRODUCT;
import static org.apache.geode.benchmark.topology.Roles.rolesFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.perftest.TestConfig;

class GcParametersTest {
  private static final String WITH_GC = "benchmark.withGc";
  private static final String XX_UseZGC = "-XX:+UseZGC";
  private static final String XX_UseG1GC = "-XX:+UseG1GC";
  private static final String XX_UseConcMarkSweepGC = "-XX:+UseConcMarkSweepGC";
  private static final String XX_UseShenandoahGC = "-XX:+UseShenandoahGC";
  private static final String XX_UseEpsilonGC = "-XX:+UseEpsilonGC";

  @Test
  @ClearSystemProperty(key = WITH_GC)
  public void withDefault() {
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertCms(testConfig);
  }

  @Test
  @SetSystemProperty(key = WITH_GC, value = "CMS")
  public void withCms() {
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertCms(testConfig);
  }

  @Test
  @SetSystemProperty(key = WITH_GC, value = "G1")
  public void withG1() {
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertG1(testConfig);
  }

  @Test
  @SetSystemProperty(key = WITH_GC, value = "Z")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_11)
  public void withZ() {
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertZ(testConfig);
  }

  @Test
  @SetSystemProperty(key = WITH_GC, value = "Z")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_8)
  public void withZinJava8() {
    final TestConfig testConfig = new TestConfig();
    assertThatThrownBy(() -> GcParameters.configure(testConfig))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @SetSystemProperty(key = WITH_GC, value = "Shenandoah")
  public void withShenandoah() {
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertShenandoah(testConfig);
  }

  @Test
  @SetSystemProperty(key = WITH_GC, value = "Epsilon")
  public void withEpsilon() {
    final TestConfig testConfig = new TestConfig();
    GcParameters.configure(testConfig);
    assertEpsilon(testConfig);
  }

  private void assertCms(TestConfig testConfig) {
    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains(XX_UseConcMarkSweepGC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseG1GC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseZGC);
    });
  }

  private void assertG1(TestConfig testConfig) {
    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains(XX_UseG1GC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseConcMarkSweepGC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseZGC);
    });
  }

  private void assertZ(TestConfig testConfig) {
    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains(XX_UseZGC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseConcMarkSweepGC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseG1GC);
    });
  }

  private void assertShenandoah(TestConfig testConfig) {
    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains(XX_UseShenandoahGC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseConcMarkSweepGC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseG1GC);
    });
  }

  private void assertEpsilon(TestConfig testConfig) {
    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains(XX_UseEpsilonGC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseConcMarkSweepGC);
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XX_UseG1GC);
    });
  }

}
