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

package org.apache.geode.perftest.yardstick;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.WorkloadDuration;

public class YardstickTaskTest {

  @Test
  public void testExecuteBenchmark() throws Exception {
    EmptyBenchmark benchmark = new EmptyBenchmark();
    Task task = new YardstickTask(benchmark, new WorkloadDuration());
    task.run(null);

    Assert.assertTrue(1 <= benchmark.invocations.get());

    //TODO -verify probes are shutdown
    //TODO -verify benchmark is shutdown
    //TODO - pass in probes to yardstick util, turn it into a real class

  }

  private static class EmptyBenchmark extends BenchmarkDriverAdapter {
    private AtomicInteger invocations = new AtomicInteger();

    @Override
    public boolean test(Map<Object, Object> ctx) throws Exception {
      invocations.incrementAndGet();
      return true;
    }

    @Override
    public void onException(Throwable e) {
      e.printStackTrace();
    }
  }
}