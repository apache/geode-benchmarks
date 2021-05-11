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

package org.apache.geode.benchmark.topology.redis;

import static org.apache.geode.benchmark.tests.GeodeBenchmark.WITH_REPLICAS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.benchmark.tasks.redis.CreateRedisCluster;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestStep;

public class RedisTopologyTest {

  @Test
  @ClearSystemProperty(key = WITH_REPLICAS)
  public void createRedisClusterDefaultReplicas() {
    final TestConfig testConfig = new TestConfig();
    RedisTopology.configure(testConfig);
    assertThat(testConfig.getBefore().stream().map(TestStep::getTask)
        .filter(CreateRedisCluster.class::isInstance).map(CreateRedisCluster.class::cast)
        .findFirst()).hasValueSatisfying(t -> assertThat(t.getReplicas()).isEqualTo(1));
  }

  @Test
  @SetSystemProperty(key = WITH_REPLICAS, value = "3")
  public void createRedisClusterSetsReplicas() {
    final TestConfig testConfig = new TestConfig();
    RedisTopology.configure(testConfig);
    assertThat(testConfig.getBefore().stream().map(TestStep::getTask)
        .filter(CreateRedisCluster.class::isInstance).map(CreateRedisCluster.class::cast)
        .findFirst()).hasValueSatisfying(t -> assertThat(t.getReplicas()).isEqualTo(3));
  }

}
