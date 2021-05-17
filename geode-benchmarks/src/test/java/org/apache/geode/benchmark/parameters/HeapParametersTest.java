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

import static org.apache.geode.benchmark.topology.RoleKinds.GEODE_PRODUCT;
import static org.apache.geode.benchmark.topology.Roles.rolesFor;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.perftest.TestConfig;

class HeapParametersTest {

  private static final String WITH_HEAP = "benchmark.withHeap";

  @Test
  @ClearSystemProperty(key = WITH_HEAP)
  public void withDefault() {
    final TestConfig testConfig = new TestConfig();
    HeapParameters.configure(testConfig);
    assertHeap(testConfig, "8g");
  }

  @Test
  @SetSystemProperty(key = WITH_HEAP, value = "16g")
  public void with16g() {
    final TestConfig testConfig = new TestConfig();
    HeapParameters.configure(testConfig);
    assertHeap(testConfig, "16g");
  }

  private void assertHeap(final TestConfig testConfig, final String heap) {
    rolesFor(GEODE_PRODUCT).forEach(role -> {
      assertThat(testConfig.getJvmArgs().get(role.name())).contains("-Xmx" + heap);
      assertThat(testConfig.getJvmArgs().get(role.name())).contains("-Xms" + heap);
    });
  }
}
