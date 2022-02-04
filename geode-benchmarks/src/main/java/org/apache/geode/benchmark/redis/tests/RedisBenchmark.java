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

package org.apache.geode.benchmark.redis.tests;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Long.getLong;
import static org.apache.geode.benchmark.Config.after;
import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.redis.tests.RedisBenchmark.RedisClientImplementation.Jedis;
import static org.apache.geode.benchmark.redis.tests.RedisBenchmark.RedisClusterImplementation.Geode;
import static org.apache.geode.benchmark.redis.tests.RedisBenchmark.RedisClusterImplementation.Manual;
import static org.apache.geode.benchmark.tests.GeodeBenchmark.WITH_MAX_KEY;
import static org.apache.geode.benchmark.tests.GeodeBenchmark.WITH_MIN_KEY;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;

import org.junit.jupiter.api.Test;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.benchmark.redis.tasks.FlushDbTask;
import org.apache.geode.benchmark.redis.tasks.JedisClientManager;
import org.apache.geode.benchmark.redis.tasks.LettuceClientManager;
import org.apache.geode.benchmark.redis.tasks.RedisClientManager;
import org.apache.geode.benchmark.redis.tasks.StartRedisClient;
import org.apache.geode.benchmark.redis.tasks.StopRedisClient;
import org.apache.geode.benchmark.redis.topology.GeodeTopology;
import org.apache.geode.benchmark.redis.topology.ManualRedisTopology;
import org.apache.geode.benchmark.redis.topology.RedisTopology;
import org.apache.geode.benchmark.tests.AbstractPerformanceTest;
import org.apache.geode.benchmark.tests.GeodeBenchmark;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestRunners;

public class RedisBenchmark extends AbstractPerformanceTest {

  public static final String WITH_REDIS_CLIENT_PROPERTY = "benchmark.withRedisClient";
  public static final String WITH_REDIS_CLUSTER_PROPERTY = "benchmark.withRedisCluster";

  public static final String REDIS_SERVERS_ATTRIBUTE = "RedisBenchmark.Servers";

  public enum RedisClientImplementation {
    Jedis,
    Lettuce;

    public static RedisClientImplementation valueOfIgnoreCase(final String name) {
      for (RedisClientImplementation redisClientImplementation : RedisClientImplementation
          .values()) {
        if (redisClientImplementation.name().equalsIgnoreCase(name)) {
          return redisClientImplementation;
        }
      }
      throw new IllegalArgumentException("Unknown Redis client implementation: " + name);
    }
  }

  public enum RedisClusterImplementation {
    Geode,
    Redis,
    Manual;

    public static RedisClusterImplementation valueOfIgnoreCase(final String name) {
      for (RedisClusterImplementation redisClusterImplementation : RedisClusterImplementation
          .values()) {
        if (redisClusterImplementation.name().equalsIgnoreCase(name)) {
          return redisClusterImplementation;
        }
      }
      throw new IllegalArgumentException("Unknown Redis cluster implementation: " + name);
    }
  }

  protected transient RedisClientManager redisClientManager;

  protected LongRange keyRange =
      new LongRange(getLong(WITH_MIN_KEY, 0), getLong(WITH_MAX_KEY, 1000000));

  public void setKeyRange(final LongRange keyRange) {
    this.keyRange = keyRange;
  }

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this);
  }

  @Override
  public TestConfig configure() {

    TestConfig config = GeodeBenchmark.createConfig();

    configureClusterTopology(config);

    switch (getRedisClientImplementation()) {
      case Jedis:
        redisClientManager = new JedisClientManager();
        break;
      case Lettuce:
        redisClientManager = new LettuceClientManager();
        break;
    }

    before(config, new StartRedisClient(redisClientManager), CLIENT);

    if (Manual == getRedisClusterImplementation()) {
      before(config, new FlushDbTask(redisClientManager), CLIENT);
    }

    after(config, new StopRedisClient(redisClientManager), CLIENT);

    return config;
  }

  void configureClusterTopology(final TestConfig config) {
    switch (getRedisClusterImplementation()) {
      case Redis:
        RedisTopology.configure(config);
        break;
      case Geode:
        GeodeTopology.configure(config);
        break;
      case Manual:
        ManualRedisTopology.configure(config);
        break;
    }
  }

  RedisClientImplementation getRedisClientImplementation() {
    final String sniProp = System.getProperty(WITH_REDIS_CLIENT_PROPERTY);
    if (isNullOrEmpty(sniProp)) {
      return Jedis;
    }

    return RedisClientImplementation.valueOfIgnoreCase(sniProp);
  }

  RedisClusterImplementation getRedisClusterImplementation() {
    final String sniProp = System.getProperty(WITH_REDIS_CLUSTER_PROPERTY);
    if (isNullOrEmpty(sniProp)) {
      return Geode;
    }

    return RedisClusterImplementation.valueOfIgnoreCase(sniProp);
  }

}
