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

package org.apache.geode.perftest.yardstick;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;
import org.yardstickframework.BenchmarkProbe;
import org.yardstickframework.impl.BenchmarkLoader;
import org.yardstickframework.impl.BenchmarkProbeSet;
import org.yardstickframework.impl.BenchmarkRunner;
import org.yardstickframework.probes.DStatProbe;
import org.yardstickframework.probes.PercentileProbe;
import org.yardstickframework.probes.ThroughputLatencyProbe;
import org.yardstickframework.probes.VmStatProbe;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;
import org.apache.geode.perftest.WorkloadConfig;
import org.apache.geode.perftest.yardstick.hdrhistogram.HdrHistogramProbe;
import org.apache.geode.perftest.yardstick.hdrhistogram.HdrHistogramWriter;

/**
 * A {@link Task} that wraps a yardstick {@link BenchmarkDriver}. When the task
 * is executed, it will use yardstick to run an measure the driver.
 */
public class YardstickTask implements Task {
  public static final String YARDSTICK_OUTPUT = "-yardstick-output";
  private final BenchmarkDriver benchmark;
  private WorkloadConfig workloadConfig;

  public YardstickTask(BenchmarkDriver benchmark, WorkloadConfig workloadConfig) {
    this.benchmark = benchmark;
    this.workloadConfig = workloadConfig;
  }

  @Override
  public void run(TestContext context) throws Exception {

    BenchmarkConfiguration cfg = new BenchmarkConfiguration() {
      @Override
      public List<String> driverNames() {
        return Arrays.asList(benchmark.getClass().getName());
      }

      @Override
      public long duration() {
        return workloadConfig.getDurationSeconds();
      }

      @Override
      public long warmup() {
        return workloadConfig.getWarmupSeconds();
      }

      @Override
      public int threads() {
        return workloadConfig.getThreads();
      }

      @Override
      public String outputFolder() {
        return context.getOutputDir().getAbsolutePath();
      }

      @Override
      public String defaultDescription() {
        return YARDSTICK_OUTPUT;
      }
    };
    cfg.output(System.out);

    BenchmarkDriver[] drivers = new BenchmarkDriver[] {benchmark};
    benchmark.setUp(cfg);

    TestDoneProbe testDoneProbe = new TestDoneProbe();
    Collection<BenchmarkProbe> probes =
        Arrays.asList(new HdrHistogramProbe(new HdrHistogramWriter(context.getOutputDir())),
            new ThroughputLatencyProbe(),
            new PercentileProbe(), new DStatProbe(), new VmStatProbe(),
            testDoneProbe);
    BenchmarkLoader loader = new BenchmarkLoader();
    loader.initialize(cfg);

    BenchmarkProbeSet probeSet = new BenchmarkProbeSet(benchmark, cfg, probes, loader);
    BenchmarkProbeSet[] probeSets = new BenchmarkProbeSet[] {probeSet};
    int[] weights = new int[] {1};

    BenchmarkRunner runner = new BenchmarkRunner(cfg, drivers, probeSets, weights);

    runner.runBenchmark();

    testDoneProbe.await();
  }
}
