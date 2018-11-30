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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yardstickframework.BenchmarkDriver;

import org.apache.geode.perftest.yardstick.YardstickTask;

/**
 * Declarative configuration of a test. Used by
 * {@link PerformanceTest} to define the test.
 *
 */
public class TestConfig implements Serializable {

  private final WorkloadConfig workloadConfig = new WorkloadConfig();
  private Map<String, Integer> roles = new LinkedHashMap<>();
  private Map<String, List<String>> jvmArgs = new HashMap<>();
  private List<TestStep> before = new ArrayList<>();
  private List<TestStep> workload = new ArrayList<>();
  private List<TestStep> after = new ArrayList<>();
  private String name;

  /**
   * Define a role for the test.
   *
   * @param role The name of the role
   * @param numberOfJVMs The number of JVMs that should be launched that have this role.
   */
  public void role(String role, int numberOfJVMs) {
    this.roles.put(role, numberOfJVMs);
  }


  /**
   * Add a before task to the test. Each before task is run in parallel on
   * all of the nodes that have the given role
   *
   * @param task The task to execute
   * @param roles The roles to execute the task on
   */
  public void before(Task task, String... roles) {
    before.add(new TestStep(task, roles));
  }

  /**
   * Add an after task to the test. After tasks are run in parallel on all
   * nodes that have the given roles
   */
  public void after(Task task, String... roles) {
    after.add(new TestStep(task, roles));
  }

  /**
   * Add a workload task to the test. Workload tasks are run repeatedly
   * during the workload phase, and the time to run the workload is measured.
   *
   * @param benchmark The workload to run
   * @param roles The roles to run the workload on
   */
  public void workload(BenchmarkDriver benchmark, String... roles) {
    workload.add(new TestStep(new YardstickTask(benchmark, workloadConfig), roles));
  }


  /**
   * Set the duration of the workload phase. This is the amount of time that
   * the workload tasks are actually measured. The total runtime of the test
   * is the {@link #durationSeconds(long)} + {@link #warmupSeconds(long)}
   *
   * @param durationSeconds The time in seconds for the duration phase
   */
  public void durationSeconds(long durationSeconds) {
    workloadConfig.durationSeconds(durationSeconds);
  }

  /**
   * Set the number of threads to run in each JVM for the workload test. Default
   * is twice the number of available processors.
   */
  public void threads(int threads) {
    workloadConfig.threads(threads);
  }

  /**
   * Set the duration of the warmup phase. Workload tasks are run for this amount
   * of time as a warmup, but the measurements from the tasks are discarded.
   *
   * @param warmupSeconds The time in seconds for the warmup phase
   */
  public void warmupSeconds(long warmupSeconds) {
    workloadConfig.warmupSeconds(warmupSeconds);
  }

  /**
   * Set the name of this benchmark. This name must be unique within a benchmarking run. Comparisons
   * between runs will use this name to identify the same benchmark in both runs.
   */
  public void name(String name) {
    this.name = name;
  }

  public long getDurationSeconds() {
    return workloadConfig.getDurationSeconds();
  }

  public long getWarmupSeconds() {
    return workloadConfig.getWarmupSeconds();
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

  public String getName() {
    return name;
  }

  /**
   * Return the total number of JVMs required to run this test
   */
  public int getTotalJVMs() {
    return roles.values().stream().mapToInt(Integer::intValue).sum();
  }

  /**
   * Add JVM arguments used to launch JVMs for a particular role
   *
   * If multiple calls to this method are made for the same role, the new JVM arguments
   * are appended to the existing JVM args
   */
  public void jvmArgs(String role, String... jvmArgs) {
    List<String> roleArgs = this.jvmArgs.computeIfAbsent(role, key -> new ArrayList<>());
    roleArgs.addAll(Arrays.asList(jvmArgs));
  }

  public Map<String, List<String>> getJvmArgs() {
    return Collections.unmodifiableMap(jvmArgs);
  }

  public static class TestStep {
    private final Task task;
    private final String[] roles;

    public TestStep(Task task, String[] roles) {
      if (roles == null || roles.length == 0) {
        throw new IllegalStateException("Task " + task + " must be assigned to at least one role");
      }
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
