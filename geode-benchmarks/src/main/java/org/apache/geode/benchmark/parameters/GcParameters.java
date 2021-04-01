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

import static org.apache.geode.benchmark.parameters.JavaVersion.v11;
import static org.apache.geode.benchmark.parameters.Utils.configureGeodeProductJvms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.TestConfig;

public class GcParameters {
  private static final Logger logger = LoggerFactory.getLogger(GcParameters.class);

  public static void configure(final TestConfig testConfig) {
    final GcImplementation gcImplementation =
        GcImplementation.valueOf(System.getProperty("benchmark.withGc", "CMS"));
    logger.info("Configuring {} GC.", gcImplementation);
    switch (gcImplementation) {
      case CMS:
        configureCms(testConfig);
        break;
      case G1:
        configureG1(testConfig);
        break;
      case Z:
        configureZ(testConfig);
        break;
      case Shenandoah:
        configureShenandoah(testConfig);
        break;
    }
  }

  private static void configureShenandoah(final TestConfig testConfig) {
    configureGeodeProductJvms(testConfig,
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseShenandoahGC",
        "-XX:+AlwaysPreTouch",
        "-XX:+UseNUMA");
  }

  private static void configureZ(final TestConfig testConfig) {
    final JavaVersion javaVersion = JavaVersion.current();
    if (javaVersion.olderThan(v11)) {
      throw new IllegalArgumentException("ZGC requires Java 11 or newer");
    }
    configureGeodeProductJvms(testConfig,
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseZGC");
  }

  private static void configureG1(final TestConfig testConfig) {
    configureGeodeProductJvms(testConfig,
        "-XX:+UseG1GC",
        "-XX:+UseNUMA",
        "-XX:+ScavengeBeforeFullGC");
  }

  private static void configureCms(final TestConfig testConfig) {
    configureGeodeProductJvms(testConfig,
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
