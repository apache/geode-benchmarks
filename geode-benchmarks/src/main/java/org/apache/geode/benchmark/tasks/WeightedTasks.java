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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;

public class WeightedTasks implements BenchmarkDriver, Serializable {

  private final int totalWeight;

  public static class WeightedTask implements Serializable {
    int weight;
    BenchmarkDriver task;

    public WeightedTask(final int weight, final BenchmarkDriver task) {
      this.weight = weight;
      this.task = task;
    }
  }

  private final WeightedTask[] weightedTasks;

  public WeightedTasks(WeightedTask... weightedTasks) {
    this.weightedTasks = weightedTasks;
    this.totalWeight = Arrays.stream(weightedTasks).mapToInt(wt -> wt.weight).sum();
  }


  @Override
  public void setUp(final BenchmarkConfiguration benchmarkConfiguration) throws Exception {
    for (final WeightedTask weightedTask : weightedTasks) {
      weightedTask.task.setUp(benchmarkConfiguration);
    }
  }

  @Override
  public boolean test(final Map<Object, Object> context) throws Exception {
    int weight = ThreadLocalRandom.current().nextInt(totalWeight) + 1;
    for (final WeightedTask weightedTask : weightedTasks) {
      weight -= weightedTask.weight;
      if (weight > 0) {
        continue;
      }

      return weightedTask.task.test(context);
    }

    return true;
  }

  @Override
  public void tearDown() throws Exception {
    for (final WeightedTask weightedTask : weightedTasks) {
      weightedTask.task.tearDown();
    }
  }

  @Override
  public String description() {
    final StringBuilder stringBuilder = new StringBuilder("Composite Task:\n");
    for (final WeightedTask weightedTask : weightedTasks) {
      stringBuilder.append(weightedTask.weight).append(" ").append(weightedTask.task.description())
          .append("\n");
    }
    return stringBuilder.toString();
  }

  @Override
  public String usage() {
    final StringBuilder stringBuilder = new StringBuilder("Composite Task: \n");
    for (final WeightedTask weightedTask : weightedTasks) {
      stringBuilder.append(weightedTask.weight).append(" ").append(weightedTask.task.usage())
          .append("\n");
    }
    return stringBuilder.toString();
  }

  @Override
  public void onWarmupFinished() {
    for (final WeightedTask weightedTask : weightedTasks) {
      weightedTask.task.onWarmupFinished();
    }
  }

  @Override
  public void onException(final Throwable e) {
    for (final WeightedTask weightedTask : weightedTasks) {
      weightedTask.task.onException(e);
    }
  }
}
