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
import static org.apache.geode.benchmark.Constants.JAVA_VERSION_12;
import static org.apache.geode.benchmark.Constants.JAVA_VERSION_13;
import static org.apache.geode.benchmark.Constants.JAVA_VERSION_8;
import static org.apache.geode.benchmark.parameters.GcLoggingParameters.WITH_GC_LOGGING;
import static org.apache.geode.benchmark.topology.RoleKinds.GEODE_PRODUCT;
import static org.apache.geode.benchmark.topology.Roles.rolesFor;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.perftest.TestConfig;

class GcLoggingParametersTest {

  @Test
  @ClearSystemProperty(key = WITH_GC_LOGGING)
  public void withDefault() {
    final TestConfig testConfig = new TestConfig();
    Utils.configureGeodeProductJvms(testConfig, "-mockArg");
    GcLoggingParameters.configure(testConfig);

    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContainSubsequence("-Xlog:gc");
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContainSubsequence("-Xloggc");
    });
  }

  @Test
  @SetSystemProperty(key = WITH_GC_LOGGING, value = "true")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_8)
  public void withJava8() {
    final TestConfig testConfig = new TestConfig();
    GcLoggingParameters.configure(testConfig);
    assertThatJava8GcLog(testConfig);
  }

  @Test
  @SetSystemProperty(key = WITH_GC_LOGGING, value = "true")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_11)
  public void withJava11() {
    final TestConfig testConfig = new TestConfig();
    GcLoggingParameters.configure(testConfig);
    assertThatJava9GcLog(testConfig);
  }

  @Test
  @SetSystemProperty(key = WITH_GC_LOGGING, value = "true")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_12)
  public void withJava12() {
    final TestConfig testConfig = new TestConfig();
    GcLoggingParameters.configure(testConfig);
    assertThatJava9GcLog(testConfig);
  }

  @Test
  @SetSystemProperty(key = WITH_GC_LOGGING, value = "true")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_13)
  public void withJava13() {
    final TestConfig testConfig = new TestConfig();
    GcLoggingParameters.configure(testConfig);
    assertThatJava9GcLog(testConfig);
  }

  private void assertThatJava8GcLog(TestConfig testConfig) {
    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains("-Xloggc:OUTPUT_DIR/gc.log");
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContainSubsequence("-Xlog:gc");
    });
  }

  private void assertThatJava9GcLog(TestConfig testConfig) {
    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains("-Xlog:gc*:OUTPUT_DIR/gc.log");
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContainSubsequence("-Xloggc");
    });
  }

}
