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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import org.yardstickframework.BenchmarkDriver;

import org.apache.geode.benchmark.tasks.WeightedTasks.WeightedTask;

class WeightedTasksTest {

  @Test
  public void eventuallyCallsBothTasks() throws Exception {
    final BenchmarkDriver task1 = mock(BenchmarkDriver.class);
    final BenchmarkDriver task2 = mock(BenchmarkDriver.class);

    final Map<Object, Object> context = new ConcurrentHashMap<>();

    final WeightedTasks weightedTasks =
        new WeightedTasks(new WeightedTask(80, task1), new WeightedTask(20, task2));

    while (mockingDetails(task1).getInvocations().isEmpty() || mockingDetails(task2)
        .getInvocations().isEmpty()) {
      weightedTasks.test(context);
    }

    verify(task1, atLeastOnce()).test(same(context));
    verify(task2, atLeastOnce()).test(same(context));
  }


  @Test
  public void callsEachTaskRoughlyRelativeToTheirWeight() throws Exception {
    final BenchmarkDriver task1 = mock(BenchmarkDriver.class);
    final BenchmarkDriver task2 = mock(BenchmarkDriver.class);

    final Map<Object, Object> context = new ConcurrentHashMap<>();

    final WeightedTasks weightedTasks =
        new WeightedTasks(new WeightedTask(80, task1), new WeightedTask(20, task2));

    final int iterations = 100000;
    for (int i = 0; i < iterations; i++) {
      weightedTasks.test(context);
    }

    final Method testMethod = BenchmarkDriver.class.getMethod("test", Map.class);

    final double count1 = mockingDetails(task1).getInvocations().stream()
        .filter(i -> i.getMethod().equals(testMethod)).count();

    final double count2 = mockingDetails(task2).getInvocations().stream()
        .filter(i -> i.getMethod().equals(testMethod)).count();

    assertThat(count1 / iterations).isCloseTo(0.80, offset(0.01));
    assertThat(count2 / iterations).isCloseTo(0.20, offset(0.01));
  }

}
