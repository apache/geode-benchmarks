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

package org.apache.geode.benchmark.tasks.redis;


import static org.apache.geode.benchmark.topology.Ports.REDIS_PORT;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.tests.redis.RedisBenchmark;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class InitRedisServersAttribute implements Task {
  private static final Logger logger = LoggerFactory.getLogger(InitRedisServersAttribute.class);

  final Collection<InetSocketAddress> servers;

  public InitRedisServersAttribute() {
    this(null);
  }

  public InitRedisServersAttribute(final Collection<InetSocketAddress> servers) {
    this.servers = servers;
  }

  public Collection<InetSocketAddress> getServers() {
    return servers;
  }

  @Override
  public void run(final TestContext context) throws Exception {
    if (null == servers) {
      final List<InetSocketAddress> servers =
          context.getHostsForRole(SERVER.name()).stream().map(i -> InetSocketAddress
              .createUnresolved(i.getHostAddress(), REDIS_PORT)).collect(Collectors.toList());
      logger.info("Setting servers from roles: {}", servers);
      context.setAttribute(RedisBenchmark.REDIS_SERVERS_ATTRIBUTE, servers);
    } else {
      logger.info("Setting servers from fixed collection: {}", servers);
      context.setAttribute(RedisBenchmark.REDIS_SERVERS_ATTRIBUTE, servers);
    }
  }

}
