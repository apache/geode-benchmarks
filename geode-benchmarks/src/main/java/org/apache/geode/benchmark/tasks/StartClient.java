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

import static org.apache.geode.benchmark.parameters.GeodeProperties.clientProperties;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Properties;

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
  protected int locatorPort;

  public StartClient(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    InetAddress locator = context.getHostsForRole(LOCATOR.name()).iterator().next();

    String statsFile = new File(context.getOutputDir(), "stats.gfs").getAbsolutePath();
    Properties properties = clientProperties();

    ClientCache clientCache = createClientCacheFactory(locator, statsFile, properties, context)
        .create();

    context.setAttribute("CLIENT_CACHE", clientCache);
  }

  /**
   * Create and configure the ClientCacheFactory.
   *
   * Subclasses can override this.
   *
   */
  protected ClientCacheFactory createClientCacheFactory(final InetAddress locator,
      final String statsFile,
      final Properties properties,
      final TestContext context)
      throws NoSuchMethodException, InvocationTargetException,
      IllegalAccessException, ClassNotFoundException {
    return new ClientCacheFactory(properties)
        .setPdxSerializer(new ReflectionBasedAutoSerializer("benchmark.geode.data.*"))
        .setPoolIdleTimeout(-1)
        .set(ConfigurationProperties.STATISTIC_ARCHIVE_FILE, statsFile)
        .addPoolLocator(locator.getHostName(), locatorPort);
  }
}
