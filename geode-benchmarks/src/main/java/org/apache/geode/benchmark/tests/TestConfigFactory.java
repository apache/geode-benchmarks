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

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Properties;

import com.google.common.base.Strings;

import org.apache.geode.perftest.TestConfig;

public class TestConfigFactory {

  private static final long WARM_UP_TIME = MINUTES.toSeconds(1);
  private static final long BENCHMARK_DURATION = MINUTES.toSeconds(5);

  private int defaultThreadCount;

  public TestConfig build() {
    TestConfig testConfig = new TestConfig();
    testConfig.warmupSeconds(WARM_UP_TIME);
    testConfig.durationSeconds(BENCHMARK_DURATION);
    testConfig.threads(numThreads());
    return testConfig;

  }

  public TestConfigFactory withDefaultThreadCount(int defaultThreadCount) {
    this.defaultThreadCount = defaultThreadCount;
    return this;
  }

  private int numThreads() {
    Properties properties = System.getProperties();
    if (Strings.isNullOrEmpty(properties.getProperty("clientThreadCount"))) {
      return this.defaultThreadCount;
    }
    return Integer.parseInt(properties.getProperty("clientThreadCount"));
  }
}
