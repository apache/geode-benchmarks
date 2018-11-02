package org.apache.geode.perftest;

import org.junit.Test;

public class TestRunnerIntegrationTest {

  @Test
  public void testBeforeWorkload() throws Exception {
    TestRunner runner = new TestRunner();

    runner.runTest(testConfig -> {
      testConfig.role("all", 2);
      testConfig.before(context -> System.out.println("hello"), "all");

    }, 1);

  }
}
