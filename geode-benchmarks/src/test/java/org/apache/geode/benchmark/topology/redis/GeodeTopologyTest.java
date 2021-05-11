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

import static org.apache.geode.benchmark.parameters.redis.NettyParameters.WITH_NETTY_THREADS;
import static org.apache.geode.benchmark.tests.GeodeBenchmark.WITH_BUCKETS;
import static org.apache.geode.benchmark.tests.GeodeBenchmark.WITH_REPLICAS;
import static org.apache.geode.benchmark.topology.Roles.SERVER;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.perftest.TestConfig;

public class GeodeTopologyTest {

  @Test
  @ClearSystemProperty(key = WITH_REPLICAS)
  @ClearSystemProperty(key = WITH_BUCKETS)
  @ClearSystemProperty(key = WITH_NETTY_THREADS)
  public void setsDefaultJvmArgs() {
    final TestConfig testConfig = new TestConfig();
    GeodeTopology.configure(testConfig);

    assertThat(testConfig.getJvmArgs().get(SERVER.name()))
        .contains("-Denable-unsupported-commands=true", "-Dredis.replicas=1",
            "-Dredis.region.buckets=128", "-Djava.lang.Integer.IntegerCache.high=128")
        .doesNotContainSubsequence("-Dio.netty.eventLoopThreads=");
  }

  @Test
  @SetSystemProperty(key = WITH_REPLICAS, value = "3")
  public void setsReplicas() {
    final TestConfig testConfig = new TestConfig();
    GeodeTopology.configure(testConfig);

    assertThat(testConfig.getJvmArgs().get(SERVER.name())).contains("-Dredis.replicas=3");
  }

  @Test
  @SetSystemProperty(key = WITH_BUCKETS, value = "123")
  public void setsBuckets() {
    final TestConfig testConfig = new TestConfig();
    GeodeTopology.configure(testConfig);

    assertThat(testConfig.getJvmArgs().get(SERVER.name()))
        .contains("-Dredis.region.buckets=123", "-Djava.lang.Integer.IntegerCache.high=123");
  }

  @Test
  @SetSystemProperty(key = WITH_NETTY_THREADS, value = "3")
  public void setsNettyThreads() {
    final TestConfig testConfig = new TestConfig();
    GeodeTopology.configure(testConfig);

    assertThat(testConfig.getJvmArgs().get(SERVER.name()))
        .contains("-Dio.netty.eventLoopThreads=3");
  }

}
