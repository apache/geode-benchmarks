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

import static org.apache.geode.benchmark.parameters.JVMParameters.JVM_ARGS;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.SERVER;

import org.bouncycastle.util.Arrays;

import org.apache.geode.benchmark.tasks.StartClient;
import org.apache.geode.benchmark.tasks.StartLocator;
import org.apache.geode.benchmark.tasks.StartServer;
import org.apache.geode.perftest.TestConfig;

public class ClientServerTopology {

  /**
   * All roles defined for the JVMs created for the benchmark
   */
  public static class Roles {
    public static final String SERVER = "server";
    public static final String CLIENT = "client";
    public static final String LOCATOR = "locator";
  }

  /**
   * The port used to create the locator for the tests
   */
  public static final int LOCATOR_PORT = 10334;

  static final int NUM_LOCATORS = 1;
  static final int NUM_SERVERS = 2;
  static final int NUM_CLIENTS = 1;

  public static void configure(TestConfig testConfig) {
    testConfig.role(LOCATOR, NUM_LOCATORS);
    testConfig.role(SERVER, NUM_SERVERS);
    testConfig.role(CLIENT, NUM_CLIENTS);

    String profilerArgument = System.getProperty("benchmark.profiler.argument");
    testConfig.jvmArgs(CLIENT, appendIfNotEmpty(JVM_ARGS, profilerArgument));
    testConfig.jvmArgs(LOCATOR, appendIfNotEmpty(JVM_ARGS, profilerArgument));
    testConfig.jvmArgs(SERVER, appendIfNotEmpty(JVM_ARGS, profilerArgument));

    testConfig.before(new StartLocator(LOCATOR_PORT), LOCATOR);
    testConfig.before(new StartServer(LOCATOR_PORT), SERVER);
    testConfig.before(new StartClient(LOCATOR_PORT), CLIENT);
  }

  private static final String[] appendIfNotEmpty(String[] a, String b) {
    if (null == b || b.length() == 0) {
      return a;
    }

    return Arrays.append(a, b);
  }
}
