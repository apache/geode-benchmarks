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

package org.apache.geode.benchmark.tasks.redis;

import static java.lang.Thread.currentThread;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class JedisClusterConnectionFactory {
  private static final Logger logger = LoggerFactory.getLogger(JedisClusterConnectionFactory.class);

  private static Set<HostAndPort> nodes;

  private static ThreadLocal<JedisCluster> jedisCluster = ThreadLocal.withInitial(() -> {
    logger.info("Setup for thread {}", currentThread().getId());

    return new JedisCluster(nodes);
  });

  public static void setNodes(Set<HostAndPort> nodes) {
    JedisClusterConnectionFactory.nodes = nodes;
  }

  public static JedisCluster getConnection() {
    return jedisCluster.get();
  }
}
