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

package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.parameters.GeodeProperties.serverProperties;

import java.io.File;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to create the server cache and start the cache server.
 */
public class StartServer implements Task {

  public static final String SERVER_CACHE = "SERVER_CACHE";
  
  private final int locatorPort;
  private final int serverPort;

  public StartServer(final int locatorPort, final int serverPort) {
    this.locatorPort = locatorPort;
    this.serverPort = serverPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    Properties properties = serverProperties();

    final CacheFactory cacheFactory = new CacheFactory(properties);
    configureCacheFactory(cacheFactory, context);
    InternalCache cache = (InternalCache) cacheFactory.create();

    final CacheServer cacheServer = configureCacheServer(cache.addCacheServer(), context);
    if (null != cacheServer) {
      cacheServer.start();
    }

    configureCache(cache, context);

    context.setAttribute(SERVER_CACHE, cache);
  }

  protected void configureCache(final InternalCache cache, final TestContext context) {}

  /**
   * Configure the {@link CacheFactory}
   *
   * Subclasses can override this. Call super first to inherit settings.
   *
   * @param cacheFactory is modified by this method!
   */
  protected CacheFactory configureCacheFactory(final CacheFactory cacheFactory,
      final TestContext context)
      throws Exception {
    String locatorString = LocatorUtil.getLocatorString(context, locatorPort);
    String statsFile = new File(context.getOutputDir(), "stats.gfs").getAbsolutePath();

    return cacheFactory
        .setPdxSerializer(new ReflectionBasedAutoSerializer("benchmark.geode.data.*"))
        .set(ConfigurationProperties.LOCATORS, locatorString)
        .set(ConfigurationProperties.NAME,
            "server-" + context.getJvmID() + "-" + InetAddress.getLocalHost())
        .set(ConfigurationProperties.STATISTIC_ARCHIVE_FILE, statsFile);
  }

  /**
   * Configure the cache server
   *
   * Subclasses can override this. Call super first to inherit settings.
   *
   * @param cacheServer is modified by this method!
   */
  protected CacheServer configureCacheServer(final CacheServer cacheServer,
      final TestContext context) {
    cacheServer.setMaxConnections(Integer.MAX_VALUE);
    cacheServer.setPort(serverPort);
    return cacheServer;
  }

}
