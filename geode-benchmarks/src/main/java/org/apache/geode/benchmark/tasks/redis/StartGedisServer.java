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
import static org.apache.geode.benchmark.topology.Ports.REDIS_PORT;

import io.netty.channel.epoll.Epoll;

import org.apache.geode.benchmark.tasks.StartServer;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.perftest.TestContext;
import org.apache.geode.redis.internal.GeodeRedisService;

public class StartGedisServer extends StartServer {

  public StartGedisServer(final int locatorPort, final int serverPort) {
    super(locatorPort, serverPort);
  }

  @Override
  protected void configureCache(final InternalCache cache, final TestContext context) {
    super.configureCache(cache, context);

    final GeodeRedisService geodeRedisService = cache.getService(GeodeRedisService.class);
    geodeRedisService.setEnableUnsupported(true);
  }

  @Override
  protected CacheFactory configureCacheFactory(final CacheFactory cacheFactory,
      final TestContext context)
      throws Exception {

    Epoll.ensureAvailability();

    return super.configureCacheFactory(cacheFactory, context)
        .set(ConfigurationProperties.REDIS_ENABLED, valueOf(true))
        .set(ConfigurationProperties.REDIS_PORT, valueOf(REDIS_PORT));
  }

  @Override
  protected CacheServer configureCacheServer(final CacheServer cacheServer,
      final TestContext context) {
    return null;
  }
}
