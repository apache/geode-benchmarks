/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.benchmark.topology;

import static org.apache.geode.benchmark.Config.after;
import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.Config.role;
import static org.apache.geode.benchmark.parameters.Utils.configureGeodeProductJvms;
import static org.apache.geode.benchmark.topology.ClientServerTopologyWithSniProxy.SniProxyImplementation.HAProxy;
import static org.apache.geode.benchmark.topology.ClientServerTopologyWithSniProxy.SniProxyImplementation.Manual;
import static org.apache.geode.benchmark.topology.Ports.LOCATOR_PORT;
import static org.apache.geode.benchmark.topology.Ports.SERVER_PORT;
import static org.apache.geode.benchmark.topology.Ports.SNI_PROXY_PORT;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.PROXY;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import com.google.common.base.Strings;

import org.apache.geode.benchmark.tasks.StartClientWithSniProxy;
import org.apache.geode.benchmark.tasks.StartEnvoy;
import org.apache.geode.benchmark.tasks.StartHAProxy;
import org.apache.geode.benchmark.tasks.StartLocator;
import org.apache.geode.benchmark.tasks.StartServer;
import org.apache.geode.benchmark.tasks.StopClient;
import org.apache.geode.benchmark.tasks.StopSniProxy;
import org.apache.geode.perftest.TestConfig;

public class ClientServerTopologyWithSniProxy extends Topology {
  public static final String WITH_SNI_PROXY_PROPERTY = "benchmark.withSniProxy";
  public static final String WITH_SNI_PROXY_IMAGE_PROPERTY = "benchmark.withSniProxyImage";

  private static final int NUM_LOCATORS = 1;
  private static final int NUM_SERVERS = 2;
  private static final int NUM_CLIENTS = 1;
  private static final int NUM_PROXIES = 1;

  public enum SniProxyImplementation {
    Manual,
    HAProxy,
    Envoy;

    public static SniProxyImplementation valueOfIgnoreCase(String name) {
      name = name.toLowerCase();
      for (SniProxyImplementation sniProxyImplementation : SniProxyImplementation.values()) {
        if (sniProxyImplementation.name().toLowerCase().equals(name)) {
          return sniProxyImplementation;
        }
      }
      throw new IllegalArgumentException();
    }
  }

  public static void configure(final TestConfig config) {
    role(config, LOCATOR, NUM_LOCATORS);
    role(config, SERVER, NUM_SERVERS);
    role(config, CLIENT, NUM_CLIENTS);
    role(config, PROXY, NUM_PROXIES);

    configureBefore(config);

    before(config, new StartClientWithSniProxy(LOCATOR_PORT, SNI_PROXY_PORT, PROXY), CLIENT);

    configureAfter(config);
  }

  protected static void configureBefore(final TestConfig config) {
    configureCommon(config);

    configureGeodeProductJvms(config, WITH_SSL_ARGUMENT);

    before(config, new StartLocator(LOCATOR_PORT), LOCATOR);
    before(config, new StartServer(LOCATOR_PORT, SERVER_PORT), SERVER);

    final String image = System.getProperty(WITH_SNI_PROXY_IMAGE_PROPERTY);
    switch (getSniProxyImplementation()) {
      case HAProxy:
        before(config, new StartHAProxy(LOCATOR_PORT, SERVER_PORT, SNI_PROXY_PORT, image), PROXY);
        break;
      case Envoy:
        before(config, new StartEnvoy(LOCATOR_PORT, SERVER_PORT, SNI_PROXY_PORT, image), PROXY);
        break;
      case Manual:
        // expect proxy already configured.
    }
  }

  protected static void configureAfter(final TestConfig config) {
    after(config, new StopClient(), CLIENT);

    if (Manual != getSniProxyImplementation()) {
      after(config, new StopSniProxy(), PROXY);
    }
  }

  private static SniProxyImplementation getSniProxyImplementation() {
    final String sniProp = System.getProperty(WITH_SNI_PROXY_PROPERTY);
    if (Strings.isNullOrEmpty(sniProp)) {
      return HAProxy;
    }

    return SniProxyImplementation.valueOfIgnoreCase(sniProp);
  }

}
