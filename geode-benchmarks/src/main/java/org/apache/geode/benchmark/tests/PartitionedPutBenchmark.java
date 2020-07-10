/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.benchmark.tests;

import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.Config.workload;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import org.junit.jupiter.api.Test;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.benchmark.tasks.CreateClientProxyRegion;
import org.apache.geode.benchmark.tasks.CreatePartitionedRegion;
import org.apache.geode.benchmark.tasks.PrePopulateRegion;
import org.apache.geode.benchmark.tasks.PutTask;
import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestRunners;

/**
 * Benchmark of puts on a partitioned region.
 */
public class PartitionedPutBenchmark implements PerformanceTest {

  private LongRange keyRange = new LongRange(0, 1_000_000);

  public PartitionedPutBenchmark() {}

  public void setKeyRange(final LongRange keyRange) {
    this.keyRange = keyRange;
  }

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this);
  }

  @Override
  public TestConfig configure() {
    TestConfig config = GeodeBenchmark.createConfig();

    before(config, new CreatePartitionedRegion(), SERVER);
    if (config.getRoles().containsKey(CLIENT)) {
      before(config, new CreateClientProxyRegion(), CLIENT);
      before(config, new PrePopulateRegion(keyRange), CLIENT);
      workload(config, new PutTask(keyRange), CLIENT);
    } else {
      before(config, new PrePopulateRegion(keyRange), SERVER);
      workload(config, new PutTask(keyRange), SERVER);
    }
    return config;
  }
}
