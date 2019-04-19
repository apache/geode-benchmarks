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

import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.SERVER;

import org.apache.geode.benchmark.tasks.CreateClientProxyRegion;
import org.apache.geode.benchmark.tasks.CreatePartitionedRegion;
import org.apache.geode.benchmark.tasks.PrePopulateRegion;
import org.apache.geode.benchmark.topology.ClientServerTopology;
import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.TestConfig;

abstract class AbstractPartitionedFunctionBenchmark implements PerformanceTest {
  private long keyRange = 1000000;

  public final void setKeyRange(long keyRange) {
    this.keyRange = keyRange;
  }

  public final long getKeyRange() {
    return keyRange;
  }

  @Override
  public TestConfig configure() {
    TestConfig config = GeodeBenchmark.createConfig();
    config.threads(Runtime.getRuntime().availableProcessors() * 4);
    ClientServerTopology.configure(config);
    config.before(new CreatePartitionedRegion(), SERVER);
    config.before(new CreateClientProxyRegion(), CLIENT);
    config.before(new PrePopulateRegion(keyRange), SERVER);
    return config;
  }
}
