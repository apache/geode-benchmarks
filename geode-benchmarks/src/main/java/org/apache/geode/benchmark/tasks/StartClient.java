package org.apache.geode.benchmark.tasks;

import java.net.InetAddress;

import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class StartClient implements Task {
  private int locatorPort;

  public StartClient(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    InetAddress locator = context.getHostsForRole("locator").iterator().next();

    ClientCache clientCache = new ClientCacheFactory()
        .addPoolLocator(locator.getHostAddress(), locatorPort)
        .create();

    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY)
        .create("region");
  }
}
