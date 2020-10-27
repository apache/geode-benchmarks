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

import static org.apache.geode.benchmark.topology.Roles.PROXY;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.perftest.TestContext;

public class StartClientSNI extends StartClient {

  private final int proxyPort;

  public StartClientSNI(final int locatorPort, final int proxyPort) {
    super(locatorPort);
    this.proxyPort = proxyPort;
  }

  @Override
  protected ClientCacheFactory createClientCacheFactory(final InetAddress locator,
      final String statsFile,
      final Properties properties,
      final TestContext context)
      throws NoSuchMethodException, InvocationTargetException,
      IllegalAccessException, ClassNotFoundException {

    final ClientCacheFactory cacheFactory =
        super.createClientCacheFactory(locator, statsFile, properties, context);

    final InetAddress proxyInetAddress =
        context.getHostsForRole(PROXY.name()).stream().findFirst().get();
    return reflectivelySetSniSocketFactory(cacheFactory, proxyInetAddress);
  }

  protected ClientCacheFactory reflectivelySetSniSocketFactory(
      final ClientCacheFactory clientCacheFactory,
      final InetAddress proxyInetAddress)
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

    final Object sniSocketFactory =
        sniStaticMethod.invoke(null, proxyInetAddress.getHostName(), proxyPort);

    final Class<?> socketFactoryClass =
        Class.forName("org.apache.geode.cache.client.SocketFactory");
    final Method setPoolSocketFactoryMethod =
        clientCacheFactory.getClass().getMethod("setPoolSocketFactory", socketFactoryClass);

    return (ClientCacheFactory) setPoolSocketFactoryMethod.invoke(clientCacheFactory,
        sniSocketFactory);
  }

}
