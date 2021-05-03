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

package org.apache.geode.benchmark.tasks;

import java.io.Serializable;
import java.util.Map;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;

public class CompositeTask implements BenchmarkDriver, Serializable {

  private final BenchmarkDriver[] benchmarkDrivers;

  public CompositeTask(BenchmarkDriver... benchmarkDrivers) {
    this.benchmarkDrivers = benchmarkDrivers;
  }


  @Override
  public void setUp(final BenchmarkConfiguration benchmarkConfiguration) throws Exception {
    for (final BenchmarkDriver benchmarkDriver : benchmarkDrivers) {
      benchmarkDriver.setUp(benchmarkConfiguration);
    }
  }

  @Override
  public boolean test(final Map<Object, Object> context) throws Exception {
    for (final BenchmarkDriver benchmarkDriver : benchmarkDrivers) {
      if (!benchmarkDriver.test(context)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public void tearDown() throws Exception {
    for (final BenchmarkDriver benchmarkDriver : benchmarkDrivers) {
      benchmarkDriver.tearDown();
    }
  }

  @Override
  public String description() {
    final StringBuilder stringBuilder = new StringBuilder("Composite Task:\n");
    for (final BenchmarkDriver benchmarkDriver : benchmarkDrivers) {
      stringBuilder.append(benchmarkDriver.description()).append("\n");
    }
    return stringBuilder.toString();
  }

  @Override
  public String usage() {
    final StringBuilder stringBuilder = new StringBuilder("Composite Task:\n");
    for (final BenchmarkDriver benchmarkDriver : benchmarkDrivers) {
      stringBuilder.append(benchmarkDriver.usage()).append("\n");
    }
    return stringBuilder.toString();
  }

  @Override
  public void onWarmupFinished() {
    for (final BenchmarkDriver benchmarkDriver : benchmarkDrivers) {
      benchmarkDriver.onWarmupFinished();
    }
  }

  @Override
  public void onException(final Throwable e) {
    for (final BenchmarkDriver benchmarkDriver : benchmarkDrivers) {
      benchmarkDriver.onException(e);
    }
  }
}
