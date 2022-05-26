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

public class SafepointLoggingParameters {
  private static final Logger logger = LoggerFactory.getLogger(SafepointLoggingParameters.class);

  public static final String WITH_SAFEPOINT_LOGGING = "benchmark.withSafepointLogging";
  public static final String XLOG_SAFEPOINT = "-Xlog:safepoint*:OUTPUT_DIR/safepoint.log";

  public static void configure(final TestConfig testConfig) {
    if (getBoolean(WITH_SAFEPOINT_LOGGING)) {
      final JavaVersion javaVersion = JavaVersion.current();
      if (javaVersion.atLeast(v11)) {
        logger.info("Configuring safepoint logging parameters for Java {}.", javaVersion);
        configureGeodeProductJvms(testConfig, XLOG_SAFEPOINT);
      }
    }
  }

}
