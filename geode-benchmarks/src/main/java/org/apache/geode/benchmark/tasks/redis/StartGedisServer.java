/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.benchmark.tasks.redis;


import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.geode.benchmark.topology.Ports.EPHEMERAL_PORT;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.tasks.StartServer;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.perftest.TestContext;
import org.apache.geode.redis.internal.GeodeRedisService;

public class StartGedisServer extends StartServer {
  private static final Logger logger = LoggerFactory.getLogger(StartGedisServer.class);

  private final int redisPort;
  private final AvailabilityZoneConfig availabilityZoneConfig;

  public StartGedisServer(final int locatorPort, final int redisPort) {
    this(locatorPort, redisPort, null);
  }

  public StartGedisServer(final int locatorPort, final int redisPort,
      final AvailabilityZoneConfig availabilityZoneConfig) {
    super(locatorPort, EPHEMERAL_PORT);

    this.redisPort = redisPort;
    this.availabilityZoneConfig = availabilityZoneConfig;
  }

  @Override
  public void run(final TestContext context) throws Exception {
    if (null != availabilityZoneConfig
        && !availabilityZoneConfig.contains(context.getJvmID(), context)) {
      return;
    }

    logger.info("Starting Gedis: port={}, availabilityZoneConfig={}", redisPort,
        availabilityZoneConfig);

    super.run(context);
  }

  @Override
  protected void configureCache(final InternalCache cache, final TestContext context) {
    super.configureCache(cache, context);

    GeodeRedisService geodeRedisService = cache.getService(GeodeRedisService.class);
    geodeRedisService.setEnableUnsupported(true);
  }

  @Override
  protected CacheFactory configureCacheFactory(final CacheFactory cacheFactory,
      final TestContext context)
      throws Exception {
    final CacheFactory cf = super.configureCacheFactory(cacheFactory, context);

    if (null != availabilityZoneConfig) {
      cf.set(ConfigurationProperties.REDUNDANCY_ZONE, availabilityZoneConfig.getName());
    }
    return cf
        .set(ConfigurationProperties.REDIS_ENABLED, valueOf(true))
        .set(ConfigurationProperties.REDIS_PORT, valueOf(redisPort));
  }


  @Override
  protected CacheServer configureCacheServer(final CacheServer cacheServer,
      final TestContext context) {
    return null;
  }

  public static class AvailabilityZoneConfig implements Serializable {
    private final String name;
    private final Set<Integer> includedServers;

    public AvailabilityZoneConfig(final String name, final int... includedServers) {
      this.name = name;
      this.includedServers = stream(includedServers).boxed().collect(toSet());
    }

    public boolean contains(final int vmId, final TestContext context) {
      final List<Integer> servers =
          context.getHostsIDsForRole(SERVER.name()).stream().sorted().collect(toList());
      final int self = context.getJvmID();
      final int position = servers.indexOf(self);
      return (includedServers.contains(position));
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return "AvailabilityZoneConfig{" +
          "name='" + name + '\'' +
          ", includedServers=" + includedServers +
          '}';
    }
  }
}
