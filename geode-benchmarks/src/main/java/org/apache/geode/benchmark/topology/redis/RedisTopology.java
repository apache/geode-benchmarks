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

import static java.lang.Integer.getInteger;
import static java.lang.String.valueOf;
import static org.apache.geode.benchmark.Config.after;
import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.Config.role;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import org.apache.geode.benchmark.tasks.redis.CreateRedisCluster;
import org.apache.geode.benchmark.tasks.redis.InitRedisServersAttribute;
import org.apache.geode.benchmark.tasks.redis.StartRedisServer;
import org.apache.geode.benchmark.tasks.redis.StopRedisServer;
import org.apache.geode.benchmark.topology.Topology;
import org.apache.geode.perftest.TestConfig;

/**
 * Redis running in containers on the provided servers.
 *
 * Locators hosts are wasted so that Redis server placement happens on the same hosts as Geode.
 */
public class RedisTopology extends Topology {
  private static final int NUM_LOCATORS = Integer.getInteger(WITH_LOCATOR_COUNT_PROPERTY , 1);
  private static final int NUM_SERVERS = Integer.getInteger(WITH_SERVER_COUNT_PROPERTY , 6);
  private static final int NUM_CLIENTS = Integer.getInteger(WITH_CLIENT_COUNT_PROPERTY , 1);

  public static void configure(TestConfig config) {
    role(config, LOCATOR, NUM_LOCATORS);
    role(config, SERVER, NUM_SERVERS);
    role(config, CLIENT, NUM_CLIENTS);

    configureCommon(config);

    before(config, new StartRedisServer(), SERVER);
    before(config, new CreateRedisCluster(getInteger("withReplicas", 1)), CLIENT);
    before(config, new InitRedisServersAttribute(), CLIENT);

    after(config, new StopRedisServer(), SERVER);
  }
}
