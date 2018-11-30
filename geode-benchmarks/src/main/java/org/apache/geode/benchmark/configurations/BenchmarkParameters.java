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
package org.apache.geode.benchmark.configurations;

public class BenchmarkParameters {

  /**
   * All roles defined for the JVMs created for the benchmark
   */
  public class Roles {
    public static final String SERVER = "server";
    public static final String CLIENT = "client";
    public static final String LOCATOR = "locator";
  }

  /**
   * The port used to create the locator for the tests
   */
  public static final int LOCATOR_PORT = 10334;

  /**
   * Key range on which all the region operations are conducted on the default runner
   */
  public static final long KEY_RANGE = 1000;

  /**
   * Warm up time for the benchmark running on the default runner
   */
  public static final int WARM_UP_TIME = 60;

  /**
   * Total duration for which the benchmark will run on the default runner
   */
  public static final int BENCHMARK_DURATION = 240;

  /**
   * String key for the server cache attribute in the TestContext's attributeTree
   */
  public static final String SERVER_CACHE = "SERVER_CACHE";

  /**
   * String key for the client cache attribute in the TestContext's attributeTree
   */
  public static final String CLIENT_CACHE = "CLIENT_CACHE";

  /**
   * Key range on which all the region operations are conducted on a minimal runner
   */
  public static final long KEY_RANGE_FOR_MINIMAL_RUNNER = 5;
}
