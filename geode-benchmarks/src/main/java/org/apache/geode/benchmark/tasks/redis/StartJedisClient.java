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

import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to create the client cache
 */
public class StartJedisClient implements Task {
  private static final Logger logger = LoggerFactory.getLogger(StartJedisClient.class);

  @Override
  public void run(final TestContext context) throws Exception {
    final Set<HostAndPort> nodes = context.getHostsForRole(SERVER.name()).stream()
        .map(i -> new HostAndPort(i.getHostAddress(), 6379)).collect(Collectors.toSet());

    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(-1);
    poolConfig.setMaxIdle(-1);
    poolConfig.setLifo(false);
    JedisClusterSingleton.instance = new JedisCluster(nodes, poolConfig);

    while (true) {
      try (final Jedis jedis = JedisClusterSingleton.instance.getConnectionFromSlot(0)) {
        logger.info("Waiting for cluster to come up.");
        final String clusterInfo = jedis.clusterInfo();
        if (clusterInfo.contains("cluster_state:ok")) {
          break;
        }
        logger.debug(clusterInfo);
      } catch (Exception e) {
        logger.info("Failed connecting.", e);
      }
    }
  }

}
