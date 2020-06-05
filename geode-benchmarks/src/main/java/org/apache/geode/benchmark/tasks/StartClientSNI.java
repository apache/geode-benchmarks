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

import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.getOffPlatformHostName;
import static org.apache.geode.benchmark.topology.Ports.SNI_PROXY_PORT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.PROXY;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.proxy.ProxySocketFactories;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.apache.geode.perftest.TestContext;

public class StartClientSNI extends StartClient {

  public StartClientSNI(final int locatorPort) {
    super(locatorPort);
  }

  @Override
  protected ClientCacheFactory createClientCacheFactory(final InetAddress locator,
      final String statsFile,
      final Properties properties,
      final TestContext context)
      throws UnknownHostException {

    final InetAddress firstLocatorAddy =
        context.getHostsForRole(LOCATOR.name()).iterator().next();
    final String offPlatformLocatorName =
        getOffPlatformHostName(context, firstLocatorAddy);
    final InetAddress proxyAddy =
        context.getHostsForRole(PROXY.name()).iterator().next();

    return new ClientCacheFactory(properties)
        .setPdxSerializer(new ReflectionBasedAutoSerializer("benchmark.geode.data.*"))
        .setPoolIdleTimeout(-1)
        .set(ConfigurationProperties.STATISTIC_ARCHIVE_FILE, statsFile)
        .addPoolLocator(offPlatformLocatorName, locatorPort)
        .setPoolSocketFactory(ProxySocketFactories.sni(
            proxyAddy.getHostAddress(),
            SNI_PROXY_PORT));
  }

}
