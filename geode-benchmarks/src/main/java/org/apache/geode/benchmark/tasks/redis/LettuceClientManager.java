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
import java.util.List;
import java.util.stream.Collectors;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LettuceClientManager implements RedisClientManager {
  private static final Logger logger = LoggerFactory.getLogger(LettuceClientManager.class);

  private static RedisClusterClient redisClusterClient;

  private static final ThreadLocal<RedisAdvancedClusterCommands<String, String>> redisAdvancedClusterCommands =
      ThreadLocal.withInitial(() -> {
        logger.info("Setup for thread {}", Thread.currentThread().getId());

        final StatefulRedisClusterConnection<String, String> redisClusterConnection =
            redisClusterClient.connect();
        redisClusterConnection.setReadFrom(ReadFrom.ANY);
        return redisClusterConnection.sync();
      });


  private static final RedisClient redisClient = new RedisClient() {
    @Override
    public String get(final String key) {
      return redisAdvancedClusterCommands.get().get(key);
    }

    @Override
    public String set(final String key, final String value) {
      return redisAdvancedClusterCommands.get().set(key, value);
    }

    @Override
    public String hget(final String key, final String field) {
      return redisAdvancedClusterCommands.get().hget(key, field);
    }

    @Override
    public boolean hset(final String key, final String field, final String value) {
      return redisAdvancedClusterCommands.get().hset(key, field, value);
    }

    @Override
    public void flushdb() {
      redisAdvancedClusterCommands.get().flushdb();
    }
  };

  @Override
  public void connect(final Collection<InetSocketAddress> servers) {
    logger.info("Connect RedisClient on thread {}.", currentThread());

    final List<RedisURI> nodes = servers.stream()
        .map(i -> RedisURI.create(i.getHostString(), i.getPort())).collect(Collectors.toList());

    final RedisClusterClient redisClusterClient = RedisClusterClient.create(nodes);

    long start = System.nanoTime();
    while (true) {
      try (final StatefulRedisClusterConnection<String, String> connection =
          redisClusterClient.connect()) {
        logger.info("Waiting for cluster to come up.");
        final String clusterInfo = connection.sync().clusterInfo();
        if (clusterInfo.contains("cluster_state:ok")) {
          break;
        }
        logger.debug(clusterInfo);
      } catch (Exception e) {
        if(System.nanoTime() - start > CONNECT_TIMEOUT.toNanos()) {
          throw e;
        }
        logger.info("Failed connecting.", e);
      }
    }

    redisClusterClient.refreshPartitions();

    LettuceClientManager.redisClusterClient = redisClusterClient;
  }

  @Override
  public void close() {
    logger.info("Close RedisClient on thread {}.", currentThread());

    redisClusterClient.shutdown();
  }

  @Override
  public RedisClient get() {
    logger.info("Getting RedisClient on thread {}.", currentThread());

    return redisClient;
  }
}
