package org.apache.geode.benchmark.tests;

import org.junit.Test;

import org.apache.geode.benchmark.tasks.PutTask;
import org.apache.geode.benchmark.tasks.StartClient;
import org.apache.geode.benchmark.tasks.StartLocator;
import org.apache.geode.benchmark.tasks.StartServer;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestRunners;

public class PartitionedPutBenchmark {

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this::configure,3);
  }

  public void configure(TestConfig config) {

    int locatorPort = 10334;

    config.role("locator", 1);
    config.role("server", 1);
    config.role("client", 1);
    config.before(new StartLocator(locatorPort), "locator");
    config.before(new StartServer(locatorPort), "server");
    config.before(new StartClient(locatorPort), "client");
    config.workload(new PutTask(), 5, 30, "client");
  }
}
