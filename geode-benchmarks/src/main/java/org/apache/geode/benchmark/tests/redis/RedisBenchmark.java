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
import static org.apache.geode.benchmark.tests.redis.RedisBenchmark.RedisServerImplementation.Redis;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;

import org.apache.geode.benchmark.tasks.redis.JedisClientManager;
import org.apache.geode.benchmark.tasks.redis.LettuceClientManager;
import org.apache.geode.benchmark.tasks.redis.RedisClientManager;
import org.apache.geode.benchmark.tasks.redis.StartRedisClient;
import org.apache.geode.benchmark.tasks.redis.StopRedisClient;
import org.apache.geode.benchmark.tests.GeodeBenchmark;
import org.apache.geode.benchmark.topology.GedisTopology;
import org.apache.geode.benchmark.topology.RedisTopology;
import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.TestConfig;

public class RedisBenchmark implements PerformanceTest {

  public static final String WITH_REDIS_CLIENT_PROPERTY = "withRedisClient";
  public static final String WITH_REDIS_SERVER_PROPERTY = "withRedisServer";

  private static final int NUM_SERVERS = 6;
  private static final int NUM_CLIENTS = 1;

  public enum RedisClientImplementation {
    Jedis,
    Luttuce;

    public static RedisClientImplementation valueOfIgnoreCase(final String name) {
      for (RedisClientImplementation redisClientImplementation : RedisClientImplementation
          .values()) {
        if (redisClientImplementation.name().equalsIgnoreCase(name)) {
          return redisClientImplementation;
        }
      }
      throw new IllegalArgumentException();
    }
  }

  public enum RedisServerImplementation {
    Redis,
    Geode;

    public static RedisServerImplementation valueOfIgnoreCase(final String name) {
      for (RedisServerImplementation redisServerImplementation : RedisServerImplementation
          .values()) {
        if (redisServerImplementation.name().equalsIgnoreCase(name)) {
          return redisServerImplementation;
        }
      }
      throw new IllegalArgumentException();
    }
  }

  protected transient RedisClientManager redisClientManager;

  @Override
  public TestConfig configure() {
    TestConfig config = GeodeBenchmark.createConfig();

    switch (getRedisServerImplementation()) {
      case Redis:
        RedisTopology.configure(config);
        break;
      case Geode:
        GedisTopology.configure(config);
        break;
    }

    switch (getRedisClientImplementation()) {
      case Jedis:
        redisClientManager = new JedisClientManager();
        break;
      case Luttuce:
        redisClientManager = new LettuceClientManager();
        break;
    }

    before(config, new StartRedisClient(redisClientManager), CLIENT);
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

  private RedisServerImplementation getRedisServerImplementation() {
    final String sniProp = System.getProperty(WITH_REDIS_SERVER_PROPERTY);
    if (isNullOrEmpty(sniProp)) {
      return Redis;
    }

    return RedisServerImplementation.valueOfIgnoreCase(sniProp);
  }

}
