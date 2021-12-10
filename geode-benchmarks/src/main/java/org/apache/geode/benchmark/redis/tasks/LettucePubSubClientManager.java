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
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.cluster.pubsub.api.sync.RedisClusterPubSubCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.vavr.Function3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LettucePubSubClientManager implements RedisClientManager {
  private static final Logger logger = LoggerFactory.getLogger(LettucePubSubClientManager.class);

  private static RedisClusterClient redisClusterClient;

  private static final ThreadLocal<RedisClusterPubSubCommands<String, String>> redisClusterCommands =
      ThreadLocal.withInitial(() -> {
        logger.info("Setup for thread {}", Thread.currentThread().getId());

        final StatefulRedisClusterPubSubConnection<String, String> redisClusterPubSubConnection =
            redisClusterClient.connectPubSub();
        return redisClusterPubSubConnection.sync();
      });

  private static final RedisClient redisClient = new RedisClient() {
    @Override
    public String get(final String key) {
      return LettucePubSubClientManager.redisClusterCommands.get().get(key);
    }

    @Override
    public String set(final String key, final String value) {
      return LettucePubSubClientManager.redisClusterCommands.get().set(key, value);
    }

    @Override
    public String hget(final String key, final String field) {
      return LettucePubSubClientManager.redisClusterCommands.get().hget(key, field);
    }

    @Override
    public boolean hset(final String key, final String field, final String value) {
      return LettucePubSubClientManager.redisClusterCommands.get().hset(key, field, value);
    }

    @Override
    public long zadd(String key, double score, String value) {
      return LettucePubSubClientManager.redisClusterCommands.get().zadd(key, score, value);
    }

    @Override
    public long zrem(String key, String value) {
      return LettucePubSubClientManager.redisClusterCommands.get().zrem(key, value);
    }

    @Override
    public Set<String> zrange(String key, long start, long stop) {
      return new HashSet<>(
          LettucePubSubClientManager.redisClusterCommands.get().zrange(key, start, stop));
    }

    @Override
    public Set<String> zrangeByScore(String key, long start, long stop) {
      return new HashSet<>(
          LettucePubSubClientManager.redisClusterCommands.get().zrangebyscore(key,
              Range.create(start, stop)));
    }

    @Override
    public SubscriptionListener createSubscriptionListener(
        final Function3<String, String, Unsubscriber, Void> channelMessageConsumer) {
      return new LettuceSubscriptionListener(new RedisPubSubAdapter<String, String>() {
        @Override
        public void message(final String channel, final String message) {
          channelMessageConsumer.apply(channel, message,
              channels -> LettucePubSubClientManager.redisClusterCommands.get()
                  .unsubscribe(channels.toArray(new String[] {})));
        }
      });
    }

    @Override
    public void subscribe(final SubscriptionListener listener, final String... channels) {
      final StatefulRedisPubSubConnection<String, String> connection =
          LettucePubSubClientManager.redisClusterCommands.get().getStatefulConnection();

      connection.addListener(((LettuceSubscriptionListener) listener).getListener());
      LettucePubSubClientManager.redisClusterCommands.get().subscribe(channels);
    }

    @Override
    public void publish(String channel, String message) {
      LettucePubSubClientManager.redisClusterCommands.get().publish(channel, message);
    }

    @Override
    public void flushdb() {
      redisClusterCommands.get().flushdb();
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
      try (final StatefulRedisClusterPubSubConnection<String, String> connection =
          redisClusterClient.connectPubSub()) {
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

    LettucePubSubClientManager.redisClusterClient = redisClusterClient;
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

  static class LettuceSubscriptionListener implements RedisClient.SubscriptionListener {
    private final RedisPubSubListener<String, String> listener;

    public LettuceSubscriptionListener(
        RedisPubSubListener<String, String> listener) {
      this.listener = listener;
    }

    RedisPubSubListener<String, String> getListener() {
      return listener;
    }
  }
}
