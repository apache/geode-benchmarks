package org.apache.geode.perftest;

import org.junit.Test;

import org.apache.geode.internal.cache.tier.sockets.command.Put;
import org.apache.geode.perftest.infrastructure.local.LocalInfraManager;
import org.apache.geode.perftest.jvms.JVMManager;

public class PartitionedPutBenchmark {

  @Test
  public void run() throws Exception {

    new TestRunner(new LocalInfraManager(), new JVMManager())
        .runTest(this::configure,3);

  }

  public void configure(TestConfig config) {
    config.role("locator", 1);
    config.role("server", 1);
    config.role("client", 1);
    config.before(new StartLocator(), "locator");
    config.before(new StartServer(), "server");
    config.before(new StartClient(), "client");
    config.workload(new Put(), "client");
  }
}
