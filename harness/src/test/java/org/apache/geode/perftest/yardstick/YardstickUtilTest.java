package org.apache.geode.perftest.yardstick;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.perftest.Task;

public class YardstickUtilTest {

  @Test
  public void testExecuteBenchmark() throws Exception {
    EmptyBenchmark benchmark = new EmptyBenchmark();
    Task task = YardstickUtil.toTask(benchmark, 1, 1);
    task.run(null);

    Assert.assertTrue(1 >= benchmark.invocations);

    //TODO -verify probes are shutdown
    //TODO -verify benchmark is shutdown
    //TODO - pass in probes to yardstick util, turn it into a real class

  }

  private static class EmptyBenchmark extends BenchmarkDriverAdapter {
    private int invocations;

    @Override
    public boolean test(Map<Object, Object> ctx) throws Exception {
      invocations++;
      return true;
    }

    @Override
    public void onException(Throwable e) {
      e.printStackTrace();
    }
  }
}