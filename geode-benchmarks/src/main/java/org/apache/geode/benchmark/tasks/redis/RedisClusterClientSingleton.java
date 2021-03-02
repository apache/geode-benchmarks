package org.apache.geode.benchmark.tasks.redis;

import io.lettuce.core.cluster.RedisClusterClient;

public class RedisClusterClientSingleton {
  public static RedisClusterClient instance;
}
