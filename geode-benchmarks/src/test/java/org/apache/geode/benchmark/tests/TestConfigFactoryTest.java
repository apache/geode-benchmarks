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

package org.apache.geode.benchmark.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestConfig;

public class TestConfigFactoryTest {

  @Test
  void createConfigWithDefault() {
    TestConfig config = new TestConfigFactory().withDefaultThreadCount(500).build();

    assertThat(config.getThreads()).isEqualTo(500);
  }

  @Test
  void createConfigWithClientThreadCountPropertiesSet() {
    pushSystemProperty("workloadThreadCount", "3", () -> {
      TestConfig config = new TestConfigFactory().withDefaultThreadCount(500).build();
      assertThat(config.getThreads()).isEqualTo(3);
    });
  }

  private static void pushSystemProperty(final String key, final String value, final Runnable run) {
    final String original = System.setProperty(key, value);
    try {
      run.run();
    } finally {
      if (null == original) {
        System.clearProperty(key);
      } else {
        System.setProperty(key, original);
      }
    }
  }
}
