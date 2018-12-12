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

import java.io.File;
import java.net.InetAddress;

import org.apache.geode.benchmark.parameters.GeodeProperties;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to create the client cache
 */
public class StartClient implements Task {
  private int locatorPort;

  public StartClient(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    InetAddress locator = context.getHostsForRole("locator").iterator().next();

    String statsFile = new File(context.getOutputDir(), "stats.gfs").getAbsolutePath();

    ClientCache clientCache = new ClientCacheFactory(GeodeProperties.clientProperties())
        .setPdxSerializer(new ReflectionBasedAutoSerializer("org.apache.benchmark.geode.data.*"))
        .addPoolLocator(locator.getHostAddress(), locatorPort)
        .set(ConfigurationProperties.STATISTIC_ARCHIVE_FILE, statsFile)
        .create();

    context.setAttribute("CLIENT_CACHE", clientCache);
  }
}
