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
import static org.apache.geode.benchmark.topology.ClientServerTopologyWithRouterAndSniProxy.RouterImplementation.HAProxy;
import static org.apache.geode.benchmark.topology.ClientServerTopologyWithRouterAndSniProxy.RouterImplementation.Manual;
import static org.apache.geode.benchmark.topology.Ports.LOCATOR_PORT;
import static org.apache.geode.benchmark.topology.Ports.SNI_PROXY_PORT;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.PROXY;
import static org.apache.geode.benchmark.topology.Roles.ROUTER;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import com.google.common.base.Strings;

import org.apache.geode.benchmark.tasks.StartClientWithSniProxy;
import org.apache.geode.benchmark.tasks.StartRouter;
import org.apache.geode.benchmark.tasks.StopRouter;
import org.apache.geode.perftest.TestConfig;

public class ClientServerTopologyWithRouterAndSniProxy extends ClientServerTopologyWithSniProxy {
  public static final String WITH_ROUTER_PROPERTY = "benchmark.withRouter";
  public static final String WITH_ROUTER_IMAGE_PROPERTY = "benchmark.withRouterImage";

  private static final int NUM_LOCATORS = 1;
  private static final int NUM_SERVERS = 2;
  private static final int NUM_CLIENTS = 1;
  private static final int NUM_PROXIES = 1;
  private static final int NUM_ROUTERS = 1;

  public enum RouterImplementation {
    Manual,
    HAProxy;

    public static RouterImplementation valueOfIgnoreCase(String name) {
      name = name.toLowerCase();
      for (RouterImplementation routerImplementation : RouterImplementation.values()) {
        if (routerImplementation.name().toLowerCase().equals(name)) {
          return routerImplementation;
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
    role(config, ROUTER, NUM_ROUTERS);

    configureBefore(config);

    final String image = System.getProperty(WITH_ROUTER_IMAGE_PROPERTY);
    switch (getRouterImplementation()) {
      case HAProxy:
        before(config, new StartRouter(SNI_PROXY_PORT, image), ROUTER);
        break;
      case Manual:
        // expect router already configured
    }

    before(config, new StartClientWithSniProxy(LOCATOR_PORT, SNI_PROXY_PORT, ROUTER), CLIENT);

    configureAfter(config);

    if (Manual != getRouterImplementation()) {
      after(config, new StopRouter(), ROUTER);
    }
  }

  private static RouterImplementation getRouterImplementation() {
    final String router = System.getProperty(WITH_ROUTER_PROPERTY);
    if (Strings.isNullOrEmpty(router)) {
      return HAProxy;
    }

    return RouterImplementation.valueOfIgnoreCase(router);
  }

}
