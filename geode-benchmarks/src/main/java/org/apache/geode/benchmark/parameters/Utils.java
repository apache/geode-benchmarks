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

package org.apache.geode.benchmark.parameters;

import static org.apache.geode.benchmark.Config.jvmArgs;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.PROXY;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.TestConfig;

public class Utils {

  static final Logger logger =
      LoggerFactory.getLogger(Utils.class);

  private Utils() {}

  public static void configureJavaRoles(TestConfig config, String... args) {
    jvmArgs(config, LOCATOR, args);
    jvmArgs(config, SERVER, args);
    jvmArgs(config, CLIENT, args);
    jvmArgs(config, PROXY, args);
  }

  public static void addToTestConfig(TestConfig testConfig, String systemPropertyKey,
      String jvmArgument) {
    if (Boolean.getBoolean(systemPropertyKey)) {
      logger.info("Configuring JVMs to run with " + jvmArgument);
      configureJavaRoles(testConfig, jvmArgument);
    }
  }
}
