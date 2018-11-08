package org.apache.geode.perftest.yardstick;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;
import org.yardstickframework.BenchmarkProbe;
import org.yardstickframework.BenchmarkProbePoint;
import org.yardstickframework.impl.BenchmarkLoader;
import org.yardstickframework.impl.BenchmarkProbeSet;
import org.yardstickframework.impl.BenchmarkRunner;
import org.yardstickframework.probes.ThroughputLatencyProbe;

import org.apache.geode.perftest.Task;

public class YardstickUtil {
  public static Task toTask(BenchmarkDriver benchmark, long warmUpSeconds, long durationSeconds) {

    return (Task) context -> {

      BenchmarkConfiguration cfg = new BenchmarkConfiguration() {
        @Override
        public List<String> driverNames() {
          return Arrays.asList(benchmark.getClass().getName());
        }

        @Override
        public long duration() {
          return durationSeconds;
        }

        @Override
        public long warmup() {
          return warmUpSeconds;
        }

        @Override
        public String outputFolder() {
          return "output";
        }
      };
      cfg.output(System.out);


      BenchmarkDriver[] drivers = new BenchmarkDriver[]{benchmark};
      benchmark.setUp(cfg);

      TestDoneProbe testDoneProbe = new TestDoneProbe();
      Collection<BenchmarkProbe> probes = Arrays.asList(new ThroughputLatencyProbe(),
          testDoneProbe);
      BenchmarkLoader loader = new BenchmarkLoader();
      loader.initialize(cfg);

      BenchmarkProbeSet probeSet = new BenchmarkProbeSet(benchmark, cfg, probes, loader);
      BenchmarkProbeSet[] probeSets = new BenchmarkProbeSet[]{probeSet};
      int[] weights = new int[]{1};

      BenchmarkRunner runner = new BenchmarkRunner(cfg, drivers, probeSets, weights);

      runner.runBenchmark();

      testDoneProbe.await();
    };

  }

  private static class TestDoneProbe implements BenchmarkProbe {

    CountDownLatch done = new CountDownLatch(1);

    public void await() throws InterruptedException {
      done.await();
    }

    @Override
    public void start(BenchmarkDriver drv, BenchmarkConfiguration cfg) throws Exception {

    }

    @Override
    public void stop() throws Exception {
      done.countDown();
    }

    @Override
    public Collection<String> metaInfo() {
      return null;
    }

    @Override
    public Collection<BenchmarkProbePoint> points() {
      return Collections.emptyList();
    }

    @Override
    public void buildPoint(long time) {

    }
  }
}
