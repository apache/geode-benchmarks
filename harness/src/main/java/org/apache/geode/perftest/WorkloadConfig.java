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

package org.apache.geode.perftest;

import java.io.Serializable;

import org.yardstickframework.BenchmarkDriver;

import org.apache.geode.perftest.yardstick.YardstickTask;

/**
 * Holder for the durations of the benchmark. This is a separate object so that
 * a {@link YardstickTask} can be created with a {@link WorkloadConfig}, but the
 * actual duration values can be configured by the user after they call the
 * {@link TestConfig#workload(BenchmarkDriver, String...)} method.
 */
public class WorkloadConfig implements Serializable {
  long durationSeconds = 1;
  long warmupSeconds = 0;
  int threads = Runtime.getRuntime().availableProcessors() * 2;

  public WorkloadConfig() {}

  public void durationSeconds(long durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  public void warmupSeconds(long warmupSeconds) {
    this.warmupSeconds = warmupSeconds;
  }

  public void threads(int threads) {
    this.threads = threads;
  }

  public long getDurationSeconds() {
    return durationSeconds;
  }

  public long getWarmupSeconds() {
    return warmupSeconds;
  }

  public int getThreads() {
    return threads;
  }
}
