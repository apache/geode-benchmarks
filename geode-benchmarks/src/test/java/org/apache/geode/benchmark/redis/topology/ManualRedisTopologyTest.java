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

package org.apache.geode.benchmark.redis.topology;

import static java.net.InetSocketAddress.createUnresolved;
import static org.apache.geode.benchmark.redis.topology.ManualRedisTopology.WITH_REDIS_SERVERS_PROPERTY;
import static org.apache.geode.benchmark.topology.Ports.REDIS_PORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.benchmark.redis.tasks.InitRedisServersAttribute;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestStep;

public class ManualRedisTopologyTest {

  @Test
  @SetSystemProperty(key = WITH_REDIS_SERVERS_PROPERTY, value = "a")
  public void configureWithOneServer() {
    final TestConfig testConfig = new TestConfig();
    ManualRedisTopology.configure(testConfig);
    assertThat(testConfig.getBefore().stream().map(TestStep::getTask)
        .filter(InitRedisServersAttribute.class::isInstance)
        .map(InitRedisServersAttribute.class::cast)
        .findFirst()).hasValueSatisfying(t -> assertThat(t.getServers()).containsExactly(
            createUnresolved("a", REDIS_PORT)));
  }

  @Test
  @SetSystemProperty(key = WITH_REDIS_SERVERS_PROPERTY, value = "a;b;c")
  public void configureWithMultipleServer() {
    final TestConfig testConfig = new TestConfig();
    ManualRedisTopology.configure(testConfig);
    assertThat(testConfig.getBefore().stream().map(TestStep::getTask)
        .filter(InitRedisServersAttribute.class::isInstance)
        .map(InitRedisServersAttribute.class::cast)
        .findFirst()).hasValueSatisfying(t -> assertThat(t.getServers()).containsExactly(
            createUnresolved("a", REDIS_PORT), createUnresolved("b", REDIS_PORT),
            createUnresolved("c", REDIS_PORT)));
  }

  @Test
  @ClearSystemProperty(key = WITH_REDIS_SERVERS_PROPERTY)
  public void configureWithNoServersThrows() {
    final TestConfig testConfig = new TestConfig();
    assertThatThrownBy(() -> ManualRedisTopology.configure(testConfig))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @SetSystemProperty(key = WITH_REDIS_SERVERS_PROPERTY, value = "")
  public void configureWithNoEmptyThrows() {
    final TestConfig testConfig = new TestConfig();
    assertThatThrownBy(() -> ManualRedisTopology.configure(testConfig))
        .isInstanceOf(IllegalArgumentException.class);
  }

}
