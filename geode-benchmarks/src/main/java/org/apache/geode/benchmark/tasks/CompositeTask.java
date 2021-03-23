package org.apache.geode.benchmark.tasks;

import java.io.Serializable;
import java.util.Map;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;

public class CompositeTask implements BenchmarkDriver, Serializable {

  private final BenchmarkDriver[] benchmarkDrivers;

  public CompositeTask(BenchmarkDriver ... benchmarkDrivers) {
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
    final StringBuilder stringBuilder = new StringBuilder("Composite Task: ");
    for (final BenchmarkDriver benchmarkDriver : benchmarkDrivers) {
      stringBuilder.append(benchmarkDriver.description()).append(" ");
    }
    return stringBuilder.toString();
  }

  @Override
  public String usage() {
    final StringBuilder stringBuilder = new StringBuilder("Composite Task: \n");
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
