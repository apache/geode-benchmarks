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

import java.net.InetAddress;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

public final class JedisClientManager implements RedisClientManager {
  private static final Logger logger = LoggerFactory.getLogger(RedisClientManager.class);

  private transient JedisCluster jedisCluster;

  private final transient RedisClient redisClient = new RedisClient() {
    @Override
    public String get(final String key) {
      return jedisCluster.get(key);
    }

    @Override
    public String set(final String key, final String value) {
      return jedisCluster.set(key, value);
    }
  };

  @Override
  public void connect(final Set<InetAddress> servers) {
    logger.info("Connect RedisClient from {} on thread {}.", this, currentThread());

    final Set<HostAndPort> nodes = servers.stream()
        .map(i -> new HostAndPort(i.getHostAddress(), 6379)).collect(Collectors.toSet());

    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(-1);
    poolConfig.setMaxIdle(-1);
    poolConfig.setLifo(false);
    final JedisCluster jedisCluster = new JedisCluster(nodes, poolConfig);

    while (true) {
      try (final Jedis jedis = jedisCluster.getConnectionFromSlot(0)) {
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

    this.jedisCluster = jedisCluster;
  }

  @Override
  public void close() {
    logger.info("Close RedisClient from {} on thread {}.", this, currentThread());

    jedisCluster.close();
  }

  @Override
  public RedisClient get() {
    logger.info("Getting RedisClient from {} on thread {}.", this, currentThread());

    return redisClient;
  }
}
