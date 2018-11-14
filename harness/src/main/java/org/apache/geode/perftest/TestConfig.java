/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.perftest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yardstickframework.BenchmarkDriver;

import org.apache.geode.perftest.yardstick.YardstickTask;

/**
 * Declarative configuration of a test. Used by
 * {@link PerformanceTest} to define the test.
 */
public class TestConfig implements Serializable {

  private final WorkloadDuration workloadDuration = new WorkloadDuration();
  private Map<String, Integer> roles = new LinkedHashMap<>();
  private List<TestStep> before = new ArrayList<>();
  private List<TestStep> workload = new ArrayList<>();
  private List<TestStep> after = new ArrayList<>();

  public void role(String role, int numberOfJVMs) {
    this.roles.put(role, numberOfJVMs);
  }

  public Map<String, Integer> getRoles() {
    return roles;
  }

  public List<TestStep> getBefore() {
    return before;
  }

  public List<TestStep> getWorkload() {
    return workload;
  }

  public List<TestStep> getAfter() {
    return after;
  }

  public void before(Task task, String ... roles) {
    before.add(new TestStep(task, roles));
  }

  public void workload(BenchmarkDriver benchmark, String ... roles) {
    workload.add(new TestStep(new YardstickTask(benchmark, workloadDuration), roles));
  }

  public void after(Task task, String ... roles) {
    after.add(new TestStep(task, roles));
  }

  public void durationSeconds(long durationSeconds) {
    workloadDuration.durationSeconds(durationSeconds);
  }

  public void warmupSeconds(long warmupSeconds) {
    workloadDuration.warmupSeconds(warmupSeconds);
  }

  public long getDurationSeconds() {
    return workloadDuration.getDurationSeconds();
  }

  public long getWarmupSeconds() {
    return workloadDuration.getWarmupSeconds();
  }

  public static class TestStep {
    private final Task task;
    private final String[] roles;

    public TestStep(Task task, String[] roles) {
      this.task = task;
      this.roles = roles;
    }

    public Task getTask() {
      return task;
    }

    public String[] getRoles() {
      return roles;
    }
  }
}
