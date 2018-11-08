package org.apache.geode.perftest.yardstick;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.yardstickframework.BenchmarkDriver;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.perftest.Task;

public class YardstickUtilTest {

  @Test
  public void testExecuteBenchmark() throws Exception {
    EmptyBenchmark benchmark = new EmptyBenchmark();
    Task task = YardstickUtil.toTask(benchmark);
    task.run(null);

    Assert.assertEquals(0, benchmark.invocations);

//    verify(benchmark, atLeast(1)).test(any());

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