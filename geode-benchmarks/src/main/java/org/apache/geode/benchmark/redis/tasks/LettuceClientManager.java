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

package org.apache.geode.benchmark.redis.tasks;

import static java.lang.Thread.currentThread;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.lettuce.core.Range;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.vavr.Function3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.redis.tests.PubSubBenchmarkConfiguration;

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
    public long zadd(String key, double score, String value) {
      return redisAdvancedClusterCommands.get().zadd(key, score, value);
    }

    @Override
    public long zrem(String key, String value) {
      return redisAdvancedClusterCommands.get().zrem(key, value);
    }

    @Override
    public Set<String> zrange(String key, long start, long stop) {
      return new HashSet<>(redisAdvancedClusterCommands.get().zrange(key, start, stop));
    }

    @Override
    public Set<String> zrangeByScore(String key, long start, long stop) {
      return new HashSet<>(
          redisAdvancedClusterCommands.get().zrangebyscore(key, Range.create(start, stop)));
    }

    @Override
    public SubscriptionListener createSubscriptionListener(
        final PubSubBenchmarkConfiguration pubSubConfig,
        final Function3<String, String, Unsubscriber, Void> channelMessageConsumer) {
      throw new UnsupportedOperationException("not a pubsub client");
    }

    @Override
    public void subscribe(final SubscriptionListener listener, final String... channels) {
      throw new UnsupportedOperationException("not a pubsub client");
    }

    @Override
    public void psubscribe(final SubscriptionListener listener, final String... channelPatterns) {
      throw new UnsupportedOperationException("not a pubsub client");
    }

    @Override
    public void publish(final String channel, final String message) {
      throw new UnsupportedOperationException("not a pubsub client");
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
        if (System.nanoTime() - start > CONNECT_TIMEOUT.toNanos()) {
          throw e;
        }
        try {
          Thread.sleep(50);
        } catch (InterruptedException interruptedException) {
          throw new RuntimeException(e);
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
