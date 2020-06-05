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

import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import benchmark.geode.data.BenchmarkFunction;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.benchmark.tasks.CreateClientProxyRegion;
import org.apache.geode.benchmark.tasks.PrePopulateRegion;
import org.apache.geode.benchmark.tasks.RegisterFunction;
import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.TestConfig;

abstract class AbstractFunctionBenchmark implements PerformanceTest {
  private LongRange keyRange = new LongRange(0, 1000000);

  public final void setKeyRange(LongRange keyRange) {
    this.keyRange = keyRange;
  }

  public final LongRange getKeyRange() {
    return keyRange;
  }

  @Override
  public TestConfig configure() {
    TestConfig config = GeodeBenchmark.createConfig();
    config.threads(Runtime.getRuntime().availableProcessors() * 3);
    configureRegion(config);
    before(config, new CreateClientProxyRegion(), CLIENT);
    before(config, new PrePopulateRegion(getKeyRange()), CLIENT);
    before(config, new RegisterFunction(new BenchmarkFunction(getKeyRange())), SERVER);
    return config;
  }

  protected abstract void configureRegion(TestConfig config);
}
