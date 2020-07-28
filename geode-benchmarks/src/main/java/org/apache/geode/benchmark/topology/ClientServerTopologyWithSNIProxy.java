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
import static org.apache.geode.benchmark.topology.Ports.LOCATOR_PORT;
import static org.apache.geode.benchmark.topology.Ports.SERVER_PORT_FOR_SNI;
import static org.apache.geode.benchmark.topology.RoleKinds.GEODE_PRODUCT;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.PROXY;
import static org.apache.geode.benchmark.topology.Roles.SERVER;
import static org.apache.geode.benchmark.topology.Topology.WITH_SSL_ARGUMENT;

import java.util.stream.Stream;

import org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask;
import org.apache.geode.benchmark.tasks.StartClientSNI;
import org.apache.geode.benchmark.tasks.StartLocatorSNI;
import org.apache.geode.benchmark.tasks.StartServerSNI;
import org.apache.geode.benchmark.tasks.StartSniProxy;
import org.apache.geode.benchmark.tasks.StopClient;
import org.apache.geode.benchmark.tasks.StopSniProxy;
import org.apache.geode.perftest.TestConfig;

public class ClientServerTopologyWithSNIProxy extends Topology {
  private static final int NUM_LOCATORS = 1;
  private static final int NUM_SERVERS = 2;
  private static final int NUM_CLIENTS = 1;
  private static final int NUM_PROXIES = 1;

  public static void configure(TestConfig config) {
    role(config, LOCATOR, NUM_LOCATORS);
    role(config, SERVER, NUM_SERVERS);
    role(config, CLIENT, NUM_CLIENTS);
    role(config, PROXY, NUM_PROXIES);

    configureCommon(config);

    configureGeodeProductJvms(config, WITH_SSL_ARGUMENT);

    Stream.concat(Roles.rolesFor(GEODE_PRODUCT), Stream.of(PROXY))
        .forEach(role -> before(config, new DefineHostNamingsOffPlatformTask(), role));

    before(config, new StartLocatorSNI(LOCATOR_PORT), LOCATOR);
    before(config, new StartServerSNI(LOCATOR_PORT, SERVER_PORT_FOR_SNI), SERVER);
    before(config, new StartSniProxy(LOCATOR_PORT), PROXY);
    before(config, new StartClientSNI(LOCATOR_PORT), CLIENT);

    after(config, new StopClient(), CLIENT);
    after(config, new StopSniProxy(), PROXY);
  }

}
