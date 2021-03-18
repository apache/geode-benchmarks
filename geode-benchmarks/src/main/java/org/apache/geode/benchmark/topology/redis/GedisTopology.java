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

import static org.apache.geode.benchmark.Config.after;
import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.Config.role;
import static org.apache.geode.benchmark.topology.Ports.LOCATOR_PORT;
import static org.apache.geode.benchmark.topology.Ports.REDIS_PORT;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import org.apache.geode.benchmark.parameters.GedisParameters;
import org.apache.geode.benchmark.parameters.NettyParameters;
import org.apache.geode.benchmark.tasks.StartLocator;
import org.apache.geode.benchmark.tasks.StopLocator;
import org.apache.geode.benchmark.tasks.StopServer;
import org.apache.geode.benchmark.tasks.redis.InitRedisServersAttribute;
import org.apache.geode.benchmark.tasks.redis.StartGedisServer;
import org.apache.geode.benchmark.topology.Topology;
import org.apache.geode.perftest.TestConfig;

public class GedisTopology extends Topology {
  private static final int NUM_LOCATORS = 1;
  private static final int NUM_SERVERS = 6;
  private static final int NUM_CLIENTS = 4;

  public static void configure(TestConfig config) {
    role(config, LOCATOR, NUM_LOCATORS);
    role(config, SERVER, NUM_SERVERS);
    role(config, CLIENT, NUM_CLIENTS);

    configureCommon(config);

    NettyParameters.configure(config);
    GedisParameters.configure(config);

    before(config, new StartLocator(LOCATOR_PORT), LOCATOR);
    before(config, new StartGedisServer(LOCATOR_PORT, REDIS_PORT), SERVER);
    before(config, new InitRedisServersAttribute(), CLIENT);

    after(config, new StopServer(), SERVER);
    after(config, new StopLocator(), LOCATOR);
  }
}
