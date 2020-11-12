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
import static org.apache.geode.benchmark.topology.RoleKinds.GEODE_PRODUCT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.topology.Roles;
import org.apache.geode.perftest.TestConfig;

public class Utils {

  static final Logger logger =
      LoggerFactory.getLogger(Utils.class);

  private Utils() {}

  /**
   * We have many settings we want to apply to JVMs that are hosting Geode. Not all JVMs
   * host Geode. This method applies the setting to only the Geode product JVMs.
   */
  public static void configureGeodeProductJvms(final TestConfig config, final String... args) {
    Roles.rolesFor(GEODE_PRODUCT).forEach(role -> jvmArgs(config, role, args));
  }

  public static void addToTestConfig(TestConfig testConfig, String systemPropertyKey,
      String jvmArgument) {
    if (Boolean.getBoolean(systemPropertyKey)) {
      logger.info("Configuring JVMs to run with " + jvmArgument);
      configureGeodeProductJvms(testConfig, jvmArgument);
    }
  }

  public static void addToTestConfig(TestConfig testConfig, String systemPropertyKey) {
    if (System.getProperties().containsKey(systemPropertyKey)) {
      final String value = System.getProperty(systemPropertyKey);
      final String jvmArgument = "-D" + systemPropertyKey + "=" + value + "";
      logger.info("Configuring JVMs to run with " + jvmArgument);
      configureGeodeProductJvms(testConfig, jvmArgument);
    }
  }

}
