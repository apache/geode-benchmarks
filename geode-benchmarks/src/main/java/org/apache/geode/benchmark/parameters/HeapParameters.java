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

import static java.lang.System.getProperty;
import static org.apache.geode.benchmark.Config.jvmArgs;
import static org.apache.geode.benchmark.topology.RoleKinds.GEODE_PRODUCT;
import static org.apache.geode.benchmark.topology.Roles.rolesFor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.TestConfig;

public class HeapParameters {
  private static final Logger logger = LoggerFactory.getLogger(HeapParameters.class);

  public static void configure(final TestConfig testConfig) {
    final String defaultHeap = getProperty("benchmark.withHeap", "8g");

    rolesFor(GEODE_PRODUCT).forEach(role -> {
      final String roleName = role.toString().toLowerCase();
      final String heap = getProperty("benchmark." + roleName + ".withHeap", defaultHeap);

      logger.info("Configuring {} with heap {}.", roleName, heap);
      jvmArgs(testConfig, role, "-Xmx" + heap, "-Xms" + heap);
    });
  }

}
