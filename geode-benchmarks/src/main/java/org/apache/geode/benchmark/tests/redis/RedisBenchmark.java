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

package org.apache.geode.benchmark.tests.redis;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.geode.benchmark.Config.after;
import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.tests.redis.RedisBenchmark.RedisClientImplementation.Jedis;
import static org.apache.geode.benchmark.tests.redis.RedisBenchmark.RedisClusterImplementation.Geode;
import static org.apache.geode.benchmark.tests.redis.RedisBenchmark.RedisClusterImplementation.Manual;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;

import org.apache.geode.benchmark.tasks.redis.FlushDbTask;
import org.apache.geode.benchmark.tasks.redis.JedisClientManager;
import org.apache.geode.benchmark.tasks.redis.LettuceClientManager;
import org.apache.geode.benchmark.tasks.redis.RedisClientManager;
import org.apache.geode.benchmark.tasks.redis.StartRedisClient;
import org.apache.geode.benchmark.tasks.redis.StopRedisClient;
import org.apache.geode.benchmark.tests.GeodeBenchmark;
import org.apache.geode.benchmark.topology.redis.GedisTopology;
import org.apache.geode.benchmark.topology.redis.ManualRedisTopology;
import org.apache.geode.benchmark.topology.redis.RedisTopology;
import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.TestConfig;

public class RedisBenchmark implements PerformanceTest {

  public static final String WITH_REDIS_CLIENT_PROPERTY = "withRedisClient";
  public static final String WITH_REDIS_CLUSTER_PROPERTY = "withRedisCluster";

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

  @Override
  public TestConfig configure() {
    TestConfig config = GeodeBenchmark.createConfig();

    switch (getRedisClusterImplementation()) {
      case Redis:
        RedisTopology.configure(config);
        break;
      case Geode:
        GedisTopology.configure(config);
        break;
      case Manual:
        ManualRedisTopology.configure(config);
        break;
    }

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

  private RedisClientImplementation getRedisClientImplementation() {
    final String sniProp = System.getProperty(WITH_REDIS_CLIENT_PROPERTY);
    if (isNullOrEmpty(sniProp)) {
      return Jedis;
    }

    return RedisClientImplementation.valueOfIgnoreCase(sniProp);
  }

  private RedisClusterImplementation getRedisClusterImplementation() {
    final String sniProp = System.getProperty(WITH_REDIS_CLUSTER_PROPERTY);
    if (isNullOrEmpty(sniProp)) {
      return Geode;
    }

    return RedisClusterImplementation.valueOfIgnoreCase(sniProp);
  }

}
