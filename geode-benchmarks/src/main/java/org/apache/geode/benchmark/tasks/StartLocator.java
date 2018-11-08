package org.apache.geode.benchmark.tasks;

import org.apache.geode.distributed.Locator;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class StartLocator implements Task {
  private int locatorPort;

  public StartLocator(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {
    Locator.startLocatorAndDS(locatorPort, null, null);

  }
}
