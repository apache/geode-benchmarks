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

import org.apache.geode.perftest.TestConfig;

public class GeodeBenchmark {

  /**
   * Warm up time for the benchmark running on the default runner
   */
  private static final long WARM_UP_TIME = MINUTES.toSeconds(1);

  /**
   * Total duration for which the benchmark will run on the default runner
   */
  private static final long BENCHMARK_DURATION = MINUTES.toSeconds(5);

  /**
   * Number of threads to run benchmark.
   */
  private static final int THREADS = Runtime.getRuntime().availableProcessors() * 16;


  public static TestConfig createConfig() {
    TestConfig testConfig = new TestConfig();
    testConfig.warmupSeconds(WARM_UP_TIME);
    testConfig.durationSeconds(BENCHMARK_DURATION);
    testConfig.threads(THREADS);
    return testConfig;
  }

}
