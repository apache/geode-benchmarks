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
import static org.apache.geode.perftest.jvms.JavaVersion.v11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.topology.Roles;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.jvms.JavaVersion;

public class GcParameters {
  private static final Logger logger = LoggerFactory.getLogger(GcParameters.class);

  public static void configure(final TestConfig testConfig) {
    final String defaultGc = getProperty("benchmark.withGc", "CMS");

    rolesFor(GEODE_PRODUCT).forEach(role -> {
      final String roleName = role.toString().toLowerCase();
      final GcImplementation gcImplementation =
          GcImplementation.valueOf(getProperty("benchmark." + roleName + ".withGc", defaultGc));

      logger.info("Configuring {} with {} GC.", roleName, gcImplementation);
      switch (gcImplementation) {
        case CMS:
          configureCms(testConfig, role);
          break;
        case G1:
          configureG1(testConfig, role);
          break;
        case Z:
          configureZ(testConfig, role);
          break;
        case Shenandoah:
          configureShenandoah(testConfig, role);
          break;
        case Epsilon:
          configureEpsilon(testConfig, role);
          break;
      }

    });
  }

  private static void configureEpsilon(final TestConfig testConfig, final Roles role) {
    jvmArgs(testConfig, role,
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseEpsilonGC",
        "-XX:+UseNUMA");
  }

  private static void configureShenandoah(final TestConfig testConfig, final Roles role) {
    jvmArgs(testConfig, role,
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseShenandoahGC",
        "-XX:+AlwaysPreTouch",
        "-XX:+UseNUMA");
  }

  private static void configureZ(final TestConfig testConfig, final Roles role) {
    final JavaVersion javaVersion = JavaVersion.current();
    if (javaVersion.olderThan(v11)) {
      throw new IllegalArgumentException("ZGC requires Java 11 or newer");
    }
    jvmArgs(testConfig, role,
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseZGC");
  }

  private static void configureG1(final TestConfig testConfig, final Roles role) {
    jvmArgs(testConfig, role,
        "-XX:+UseG1GC",
        "-XX:+UseNUMA",
        "-XX:+ScavengeBeforeFullGC");
  }

  private static void configureCms(final TestConfig testConfig, final Roles role) {
    jvmArgs(testConfig, role,
        "-XX:+UseConcMarkSweepGC",
        "-XX:+UseCMSInitiatingOccupancyOnly",
        "-XX:+CMSClassUnloadingEnabled",
        "-XX:+CMSScavengeBeforeRemark",
        "-XX:CMSInitiatingOccupancyFraction=60",
        "-XX:+UseNUMA",
        "-XX:+ScavengeBeforeFullGC",
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:ParGCCardsPerStrideChunk=32768");
  }

}
