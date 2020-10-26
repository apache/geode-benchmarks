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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.geode.cache.client.ClientCacheFactory;
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
      throws UnknownHostException, NoSuchMethodException, InvocationTargetException,
      IllegalAccessException, ClassNotFoundException {

    final InetAddress firstLocatorAddy =
        context.getHostsForRole(LOCATOR.name()).iterator().next();
//    final String offPlatformLocatorName =
//        getOffPlatformHostName(context, firstLocatorAddy);
    final InetAddress proxyAddy =
        context.getHostsForRole(PROXY.name()).iterator().next();

    final ClientCacheFactory cacheFactory = new ClientCacheFactory(properties)
        .setPdxSerializer(new ReflectionBasedAutoSerializer("benchmark.geode.data.*"))
        .setPoolIdleTimeout(-1)
        .set(ConfigurationProperties.STATISTIC_ARCHIVE_FILE, statsFile)
        .addPoolLocator(firstLocatorAddy.getHostName(), locatorPort);
    final String proxyHostAddress = proxyAddy.getHostAddress();
    return reflectivelySetSniSocketFactory(cacheFactory, proxyHostAddress);
  }

  protected ClientCacheFactory reflectivelySetSniSocketFactory(
      final ClientCacheFactory clientCacheFactory,
      final String proxyHostAddress)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
      ClassNotFoundException {
    /*
     * We'd like to simply do the following, but that would introduce a compile-time dependency on
     * Geode [1.13,). But we want this benchmark code to work with older Geode version. So we'll
     * use reflection to do it.
     *
     * return clientCacheFactory
     * .setPoolSocketFactory(ProxySocketFactories.sni(
     * proxyHostAddress,
     * SNI_PROXY_PORT));
     */
    final Class<?> proxySocketFactoriesClass =
        Class.forName("org.apache.geode.cache.client.proxy.ProxySocketFactories");
    final Method sniStaticMethod =
        proxySocketFactoriesClass.getMethod("sni", String.class, int.class);

    final Object sniSocketFactory = sniStaticMethod.invoke(null, proxyHostAddress, SNI_PROXY_PORT);

    final Class<?> socketFactoryClass =
        Class.forName("org.apache.geode.cache.client.SocketFactory");
    final Method setPoolSocketFactoryMethod =
        clientCacheFactory.getClass().getMethod("setPoolSocketFactory", socketFactoryClass);

    return (ClientCacheFactory) setPoolSocketFactoryMethod.invoke(clientCacheFactory,
        sniSocketFactory);
  }

}
