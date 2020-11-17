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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to create the server cache and start the cache server.
 */
public class StartServer implements Task {

  private final int locatorPort;
  private final int serverPort;

  public StartServer(final int locatorPort, final int serverPort) {
    this.locatorPort = locatorPort;
    this.serverPort = serverPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    Properties properties = serverProperties();

    String locatorString = LocatorUtil.getLocatorString(context, locatorPort);
    String statsFile = new File(context.getOutputDir(), "stats.gfs").getAbsolutePath();
    Cache cache = new CacheFactory(properties)
        .setPdxSerializer(new ReflectionBasedAutoSerializer("benchmark.geode.data.*"))
        .set(ConfigurationProperties.LOCATORS, locatorString)
        .set(ConfigurationProperties.NAME,
            "server-" + context.getJvmID() + "-" + InetAddress.getLocalHost())
        .set(ConfigurationProperties.STATISTIC_ARCHIVE_FILE, statsFile)
        .create();
    CacheServer cacheServer = cache.addCacheServer();
    configureCacheServer(cacheServer, context);
    cacheServer.start();
    context.setAttribute("SERVER_CACHE", cache);

  }

  /**
   * Configure the cache server
   *
   * Subclasses can override this. Call super first to inherit settings.
   *
   * @param cacheServer is modified by this method!
   */
  protected void configureCacheServer(final CacheServer cacheServer, final TestContext context) {
    cacheServer.setMaxConnections(Integer.MAX_VALUE);
    cacheServer.setPort(serverPort);
  }

}
