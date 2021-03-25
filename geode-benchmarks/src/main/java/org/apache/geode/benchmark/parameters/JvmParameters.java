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

import static org.apache.geode.benchmark.parameters.Utils.configureGeodeProductJvms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.TestConfig;

public class JvmParameters {
  private static final Logger logger = LoggerFactory.getLogger(JvmParameters.class);

  public static void configure(final TestConfig testConfig) {
    logger.info("Configuring JVM parameters.");

    configureGeodeProductJvms(testConfig,
        "-server",
        "-Djava.awt.headless=true",
        "-Dsun.rmi.dgc.server.gcInterval=9223372036854775806",
        "-Dgemfire.OSProcess.ENABLE_OUTPUT_REDIRECTION=true",
        "-Dgemfire.launcher.registerSignalHandlers=true",
        "-XX:+DisableExplicitGC");

    final JavaVersion javaVersion = JavaVersion.current();
    if (javaVersion.atLeast(JavaVersion.v11)) {
      configureGeodeProductJvms(testConfig,
          "-XX:-ThreadLocalHandshakes",
      "-XX:MonitorUsedDeflationThreshold=0");
    }
  }

}
