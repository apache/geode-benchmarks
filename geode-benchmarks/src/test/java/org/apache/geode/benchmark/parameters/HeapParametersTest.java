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

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestConfig;

class HeapParametersTest {

  private static final String WITH_HEAP = "benchmark.withHeap";

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
    System.clearProperty(WITH_HEAP);
    final TestConfig testConfig = new TestConfig();
    HeapParameters.configure(testConfig);
    assertHeap(testConfig, "8g");
  }

  @Test
  public void with16g() {
    System.setProperty(WITH_HEAP, "16g");
    final TestConfig testConfig = new TestConfig();
    HeapParameters.configure(testConfig);
    assertHeap(testConfig, "16g");
  }

  private void assertHeap(final TestConfig testConfig, final String heap) {
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-Xmx" + heap);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).contains("-Xmx" + heap);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).contains("-Xmx" + heap);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-Xms" + heap);
    assertThat(testConfig.getJvmArgs().get(SERVER.name())).contains("-Xms" + heap);
    assertThat(testConfig.getJvmArgs().get(LOCATOR.name())).contains("-Xms" + heap);
  }
}
