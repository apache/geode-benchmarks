package org.apache.geode.perftest;

import java.net.InetAddress;
import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.server.CacheServer;

public class StartServer implements Task {

  private int locatorPort;

  public StartServer(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    Set<InetAddress> locators = context.getHostsForRole("locator");

    String locatorString = locators.iterator().next().toString() + "[" + locatorPort + "]";

    Cache cache = new CacheFactory()
        .set("locators",locatorString)
        .set("name","server-"+ InetAddress.getLocalHost())
        .create();
    CacheServer cacheServer = ((Cache) cache).addCacheServer();
    cacheServer.setPort(0);
    cacheServer.start();
  }
}
