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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
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

  private static JedisCluster jedisCluster;

  public static final int MAX_SLOTS = 1 << 14;

  private static final RedisClient redisClient = new RedisClient() {
    @Override
    public String get(final String key) {
      return jedisCluster.get(key);
    }

    @Override
    public String set(final String key, final String value) {
      return jedisCluster.set(key, value);
    }

    @Override
    public String hget(final String key, final String field) {
      return jedisCluster.hget(key, field);
    }

    @Override
    public boolean hset(final String key, final String field, final String value) {
      return 1 == jedisCluster.hset(key, field, value);
    }

    @Override
    public void flushdb() {
      Set<String> seen = new HashSet<>();
      for (int i = 0; i < MAX_SLOTS; ++i) {
        final Jedis connectionFromSlot = jedisCluster.getConnectionFromSlot(i);
        if (seen.add(connectionFromSlot.getClient().getHost())) {
          logger.info("Executing flushdb on {}", connectionFromSlot.getClient().getHost());
          connectionFromSlot.flushDB();
        }
      }
    }
  };

  @Override
  public void connect(final Collection<InetSocketAddress> servers) throws InterruptedException {
    logger.info("Connect RedisClient on thread {}.", currentThread());

    final Set<HostAndPort> nodes = servers.stream()
        .map(i -> new HostAndPort(i.getHostString(), i.getPort())).collect(Collectors.toSet());

    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(-1);
    poolConfig.setMaxIdle(-1);
    poolConfig.setLifo(false);
    final JedisCluster jedisCluster = new JedisCluster(nodes, Integer.MAX_VALUE, poolConfig);

    long start = System.nanoTime();
    while (true) {
      try (final Jedis jedis = jedisCluster.getConnectionFromSlot(0)) {
        logger.info("Waiting for cluster to come up.");
        final String clusterInfo = jedis.clusterInfo();
        if (clusterInfo.contains("cluster_state:ok")) {
          break;
        }
        logger.debug(clusterInfo);
      } catch (Exception e) {
        if(System.nanoTime() - start > CONNECT_TIMEOUT.toNanos()) {
          throw e;
        }
        Thread.sleep(50);
        logger.info("Failed connecting.", e);
      }
    }

    JedisClientManager.jedisCluster = jedisCluster;
  }

  @Override
  public void close() {
    logger.info("Close RedisClient on thread {}.", currentThread());

    jedisCluster.close();
  }

  @Override
  public RedisClient get() {
    logger.info("Getting RedisClient on thread {}.", currentThread());

    return redisClient;
  }
}
