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

import static org.apache.geode.benchmark.parameters.Utils.addToTestConfig;

import org.apache.geode.benchmark.parameters.GcLoggingParameters;
import org.apache.geode.benchmark.parameters.GcParameters;
import org.apache.geode.benchmark.parameters.HeapParameters;
import org.apache.geode.benchmark.parameters.JvmParameters;
import org.apache.geode.benchmark.parameters.ProfilerParameters;
import org.apache.geode.perftest.TestConfig;

public abstract class Topology {
  public static final String WITH_SSL_PROPERTY = "benchmark.withSsl";
  static final String WITH_SSL_ARGUMENT = "-Dbenchmark.withSsl=true";

  public static final String WITH_SSL_PROTOCOLS_PROPERTY = "benchmark.withSslProtocols";
  public static final String WITH_SSL_CIPHERS_PROPERTY = "benchmark.withSslCiphers";

  public static final String WITH_SECURITY_MANAGER_PROPERTY = "benchmark.withSecurityManager";
  static final String WITH_SECURITY_MANAGER_ARGUMENT = "-Dbenchmark.withSecurityManager=true";

  static void configureCommon(TestConfig config) {
    JvmParameters.configure(config);
    HeapParameters.configure(config);
    GcLoggingParameters.configure(config);
    GcParameters.configure(config);
    ProfilerParameters.configure(config);

    addToTestConfig(config, WITH_SSL_PROPERTY, WITH_SSL_ARGUMENT);
    addToTestConfig(config, WITH_SSL_PROTOCOLS_PROPERTY);
    addToTestConfig(config, WITH_SSL_CIPHERS_PROPERTY);
    addToTestConfig(config, WITH_SECURITY_MANAGER_PROPERTY, WITH_SECURITY_MANAGER_ARGUMENT);
  }

}
