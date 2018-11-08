package org.apache.geode.benchmark.tasks;

import java.net.InetAddress;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class StartServer implements Task {

  private int locatorPort;

  public StartServer(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    String locatorString = LocatorUtil.getLocatorString(context, locatorPort);

    Cache cache = new CacheFactory()
        .set("locators",locatorString)
        .set("name","server-"+ InetAddress.getLocalHost())
        .create();

    CacheServer cacheServer = ((Cache) cache).addCacheServer();
    cacheServer.setPort(0);
    cacheServer.start();

    cache.createRegionFactory(RegionShortcut.PARTITION).create("region");
  }

}
