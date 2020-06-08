/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.benchmark;

import org.yardstickframework.BenchmarkDriver;

import org.apache.geode.benchmark.topology.Roles;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestConfig;

/**
 * This is a more strongly-typed interface to the (stringly-typed) TestConfig interface in the
 * benchmark driver. This lets callers use the Roles enum instead of strings.
 */
public class Config {
  private Config() {}

  public static void role(final TestConfig config, final Roles role, final int numberOfInstances) {
    config.role(role.name(), numberOfInstances);
  }

  public static void before(final TestConfig config, final Task task, final Roles role) {
    config.before(task, role.name());
  }

  public static void workload(final TestConfig config, final BenchmarkDriver task,
      final Roles role) {
    config.workload(task, role.name());
  }

  public static void after(final TestConfig config, final Task task, final Roles role) {
    config.after(task, role.name());
  }

  public static void jvmArgs(final TestConfig config, final Roles role, final String... jvmArgs) {
    config.jvmArgs(role.name(), jvmArgs);
  }
}
