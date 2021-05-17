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
import static org.apache.geode.benchmark.parameters.SafepointLoggingParameters.WITH_SAFEPOINT_LOGGING;
import static org.apache.geode.benchmark.parameters.SafepointLoggingParameters.XLOG_SAFEPOINT;
import static org.apache.geode.benchmark.topology.RoleKinds.GEODE_PRODUCT;
import static org.apache.geode.benchmark.topology.Roles.rolesFor;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.perftest.TestConfig;

class SafepointLoggingParametersTest {

  @Test
  @ClearSystemProperty(key = WITH_SAFEPOINT_LOGGING)
  public void withDefault() {
    final TestConfig testConfig = new TestConfig();
    Utils.configureGeodeProductJvms(testConfig, "-mockArg");
    SafepointLoggingParameters.configure(testConfig);

    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XLOG_SAFEPOINT);
    });

  }

  @Test
  @SetSystemProperty(key = WITH_SAFEPOINT_LOGGING, value = "true")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_11)
  public void withSafepointLoggingJava11() {
    final TestConfig testConfig = new TestConfig();
    SafepointLoggingParameters.configure(testConfig);

    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains(XLOG_SAFEPOINT);
    });
  }

  @Test
  @SetSystemProperty(key = WITH_SAFEPOINT_LOGGING, value = "true")
  @SetSystemProperty(key = JAVA_RUNTIME_VERSION, value = JAVA_VERSION_8)
  public void withSafepointLoggingJava8() {
    final TestConfig testConfig = new TestConfig();
    Utils.configureGeodeProductJvms(testConfig, "-mockArg");
    SafepointLoggingParameters.configure(testConfig);

    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).doesNotContain(XLOG_SAFEPOINT);
    });
  }

}
