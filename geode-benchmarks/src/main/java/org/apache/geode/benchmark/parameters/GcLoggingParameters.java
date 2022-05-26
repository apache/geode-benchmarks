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

import static java.lang.Boolean.getBoolean;
import static org.apache.geode.benchmark.parameters.Utils.configureGeodeProductJvms;
import static org.apache.geode.perftest.jvms.JavaVersion.v11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.jvms.JavaVersion;

public class GcLoggingParameters {
  private static final Logger logger = LoggerFactory.getLogger(GcLoggingParameters.class);

  public static final String WITH_GC_LOGGING = "benchmark.withGcLogging";

  public static void configure(final TestConfig testConfig) {
    if (getBoolean(WITH_GC_LOGGING)) {
      final JavaVersion javaVersion = JavaVersion.current();
      logger.info("Configuring GC logging parameters for Java {}.", javaVersion);
      if (javaVersion.atLeast(v11)) {
        configureGeodeProductJvms(testConfig, "-Xlog:gc*:OUTPUT_DIR/gc.log");
      } else {
        configureGeodeProductJvms(testConfig,
            "-XX:+PrintGCDetails",
            "-XX:+PrintGCTimeStamps",
            "-XX:+PrintGCDateStamps",
            "-XX:+PrintGCApplicationStoppedTime",
            "-XX:+PrintGCApplicationConcurrentTime",
            "-XX:+UseGCLogFileRotation",
            "-XX:NumberOfGCLogFiles=20",
            "-XX:GCLogFileSize=1M",
            "-Xloggc:OUTPUT_DIR/gc.log");
      }
    }
  }
}
