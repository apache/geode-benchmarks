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

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.Config.role;
import static org.apache.geode.benchmark.topology.Ports.REDIS_PORT;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.geode.benchmark.tasks.redis.InitRedisServersAttribute;
import org.apache.geode.benchmark.topology.Topology;
import org.apache.geode.perftest.TestConfig;

public class ManualRedisTopology extends Topology {
  private static final int NUM_CLIENTS = 4;

  public static final String WITH_REDIS_SERVERS_PROPERTY = "withRedisServers";

  public static void configure(TestConfig config) {
    role(config, CLIENT, NUM_CLIENTS);

    configureCommon(config);

    // Elasticache DNS is flaky so don't cache any of it.
    config.jvmArgs(CLIENT.name(), "-Dsun.net.inetaddr.ttl=0", "-Dsun.net.inetaddr.negative.ttl=0");

    final String serversProperty = System.getProperty(WITH_REDIS_SERVERS_PROPERTY);
    if (null == serversProperty) {
      throw new IllegalArgumentException(
          WITH_REDIS_SERVERS_PROPERTY + " must be set to server address(es).");
    }

    final List<InetSocketAddress> servers = stream(serversProperty.split(";")).map(s -> {
      final String[] addressParts = s.split(":");
      return InetSocketAddress.createUnresolved(addressParts[0],
          addressParts.length == 1 ? REDIS_PORT : parseInt(addressParts[1]));
    }).collect(Collectors.toList());

    before(config, new InitRedisServersAttribute(servers), CLIENT);
  }
}
