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

import static org.apache.geode.benchmark.configurations.BenchmarkParameters.BENCHMARK_DURATION;
import static org.apache.geode.benchmark.configurations.BenchmarkParameters.KEY_RANGE;
import static org.apache.geode.benchmark.configurations.BenchmarkParameters.LOCATOR_PORT;
import static org.apache.geode.benchmark.configurations.BenchmarkParameters.Roles.CLIENT;
import static org.apache.geode.benchmark.configurations.BenchmarkParameters.Roles.LOCATOR;
import static org.apache.geode.benchmark.configurations.BenchmarkParameters.Roles.SERVER;
import static org.apache.geode.benchmark.configurations.BenchmarkParameters.WARM_UP_TIME;

import org.junit.Test;

import org.apache.geode.benchmark.tasks.CreateClientProxyRegion;
import org.apache.geode.benchmark.tasks.PrePopulateRegion;
import org.apache.geode.benchmark.tasks.StartClient;
import org.apache.geode.benchmark.tasks.StartLocator;
import org.apache.geode.benchmark.tasks.StartServer;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestRunners;

public abstract class BenchmarkOperation {
  long keyRange = KEY_RANGE;
  int warmUpTime = WARM_UP_TIME;
  int benchmarkDuration = BENCHMARK_DURATION;

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this::configure);
  }

  /**
   * This will configure a cluster of the following
   * 1 - locator
   * 4 - servers
   * 1 - client
   * Two abstract methods are also provided to be implemented to
   * create the region as required by the benchmark.
   *
   * @param config test configurations
   */
  void configure(TestConfig config) {

    int locatorPort = LOCATOR_PORT;


    config.name(this.getClass().getCanonicalName());
    config.warmupSeconds(warmUpTime);
    config.durationSeconds(benchmarkDuration);
    config.role(LOCATOR, 1);
    config.role(SERVER, 4);
    config.role(CLIENT, 1);
    config.before(new StartLocator(locatorPort), LOCATOR);
    config.before(new StartServer(locatorPort), SERVER);
    createRegion(config);
    config.before(new StartClient(locatorPort), CLIENT);
    config.before(new CreateClientProxyRegion(), CLIENT);
    config.before(new PrePopulateRegion(keyRange), SERVER);
    benchmarkOperation(config);
  }

  /**
   * The operation whose performance is to be measured
   * by the benchmark.
   *
   * @param config test configurations
   */
  protected abstract void benchmarkOperation(TestConfig config);

  /**
   * Create the region to be used in the benchmark.
   *
   * @param config test configurations.
   */
  abstract void createRegion(TestConfig config);

}
