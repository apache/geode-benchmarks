package org.apache.geode.benchmark.tasks.redis;

import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class JedisClusterSingleton {
  public static Set<HostAndPort> nodes;
}
