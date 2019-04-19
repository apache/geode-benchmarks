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

import org.junit.jupiter.api.Test;

import org.apache.geode.benchmark.tasks.ExecuteFilteredFunction;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestRunners;

public class ReplicatedFunctionExecutionWithFiltersBenchmark
    extends AbstractReplicatedFunctionBenchmark {
  private long filterKeyRange = 1000;

  public void setFilterKeyRange(long filterKeyRange) {
    this.filterKeyRange = filterKeyRange;
  }

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this);
  }

  @Override
  public TestConfig configure() {
    TestConfig config = super.configure();
    config.workload(new ExecuteFilteredFunction(getKeyRange(), filterKeyRange), CLIENT);
    return config;
  }
}
