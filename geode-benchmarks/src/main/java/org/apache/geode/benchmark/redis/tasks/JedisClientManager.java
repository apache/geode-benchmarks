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
import static redis.clients.jedis.BinaryJedisCluster.HASHSLOTS;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.vavr.Function3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import org.apache.geode.benchmark.redis.tests.PubSubBenchmarkConfiguration;

public final class JedisClientManager implements RedisClientManager {
  private static final Logger logger = LoggerFactory.getLogger(RedisClientManager.class);

  private static JedisCluster jedisCluster;

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
    public long zadd(final String key, final double score, final String value) {
      return jedisCluster.zadd(key, score, value);
    }

    @Override
    public long zrem(final String key, final String value) {
      return jedisCluster.zrem(key, value);
    }

    @Override
    public Set<String> zrange(final String key, final long start, final long stop) {
      return jedisCluster.zrange(key, start, stop);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final long start, final long stop) {
      return jedisCluster.zrangeByScore(key, start, stop);
    }

    @Override
    public SubscriptionListener createSubscriptionListener(
        final PubSubBenchmarkConfiguration pubSubConfig,
        final Function3<String, String, Unsubscriber, Void> channelMessageConsumer) {
      return new JedisSubscriptionListener(new JedisPubSub() {
        @Override
        public void onPMessage(final String pattern, final String channel, final String message) {
          super.onPMessage(pattern, channel, message);
          final Unsubscriber unsubscriber =
              channels -> punsubscribe(channels.toArray(new String[] {}));
          channelMessageConsumer.apply(channel, message, unsubscriber);
        }

        @Override
        public void onMessage(final String channel, final String message) {
          super.onMessage(channel, message);
          final Unsubscriber unsubscriber =
              channels -> unsubscribe(channels.toArray(new String[] {}));
          channelMessageConsumer.apply(channel, message, unsubscriber);
        }
      });
    }

    @Override
    public void subscribe(final SubscriptionListener listener, final String... channels) {
      jedisCluster.subscribe(((JedisSubscriptionListener) listener).getJedisPubSub(), channels);
    }

    @Override
    public void psubscribe(final SubscriptionListener listener, final String... channelPatterns) {
      jedisCluster.psubscribe(((JedisSubscriptionListener) listener).getJedisPubSub(),
          channelPatterns);
    }

    @Override
    public void publish(final String channel, final String message) {
      jedisCluster.publish(channel, message);
    }

    @Override
    public void flushdb() {
      final Set<String> seen = new HashSet<>();
      for (int i = 0; i < HASHSLOTS; ++i) {
        try (final Jedis connectionFromSlot = jedisCluster.getConnectionFromSlot(i)) {
          if (seen.add(connectionFromSlot.getClient().getHost())) {
            logger.info("Executing flushdb on {}", connectionFromSlot.getClient().getHost());
            connectionFromSlot.flushDB();
          }
        }
      }
    }
  };

  @Override
  public void connect(final Collection<InetSocketAddress> servers) {
    logger.info("Connect RedisClient on thread {}.", currentThread());

    final Set<HostAndPort> nodes = servers.stream()
        .map(i -> new HostAndPort(i.getHostString(), i.getPort())).collect(Collectors.toSet());

    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(-1);
    poolConfig.setMaxIdle(-1);
    poolConfig.setLifo(false);
    final JedisCluster jedisCluster = new JedisCluster(nodes, Integer.MAX_VALUE, poolConfig);

    final long start = System.nanoTime();
    while (true) {
      try (final Jedis jedis = jedisCluster.getConnectionFromSlot(0)) {
        logger.info("Waiting for cluster to come up.");
        final String clusterInfo = jedis.clusterInfo();
        if (clusterInfo.contains("cluster_state:ok")) {
          break;
        }
        logger.debug(clusterInfo);
      } catch (final Exception e) {
        if (System.nanoTime() - start > CONNECT_TIMEOUT.toNanos()) {
          throw e;
        }
        try {
          Thread.sleep(50);
        } catch (final InterruptedException interruptedException) {
          throw new RuntimeException(e);
        }
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

  static class JedisSubscriptionListener implements RedisClient.SubscriptionListener {
    private final JedisPubSub jedisPubSub;

    public JedisSubscriptionListener(final JedisPubSub jedisPubSub) {
      this.jedisPubSub = jedisPubSub;
    }

    JedisPubSub getJedisPubSub() {
      return jedisPubSub;
    }
  }

}
