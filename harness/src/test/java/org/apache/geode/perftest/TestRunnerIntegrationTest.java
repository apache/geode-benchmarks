package org.apache.geode.perftest;

import org.junit.Test;

import org.apache.geode.perftest.infrastructure.local.LocalInfraManager;
import org.apache.geode.perftest.jvms.JVMManager;

public class TestRunnerIntegrationTest {

  @Test
  public void runsBeforeWorkload() throws Exception {
    TestRunner runner = new TestRunner(new LocalInfraManager(), new JVMManager());

    runner.runTest(testConfig -> {
      testConfig.role("all", 1);
      testConfig.before(context -> System.out.println("hello"), "all");

    }, 1);

  }
}
