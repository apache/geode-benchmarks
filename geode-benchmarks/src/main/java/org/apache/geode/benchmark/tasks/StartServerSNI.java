package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.getOffPlatformHostName;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.perftest.TestContext;

public class StartServerSNI extends StartServer {

  public StartServerSNI(final int locatorPort) {
    super(locatorPort);
  }

  @Override
  protected void configureCacheServer(final CacheServer cacheServer, final TestContext context)
      throws UnknownHostException {
    super.configureCacheServer(cacheServer, context);
    cacheServer.setHostnameForClients(
        getOffPlatformHostName(context, InetAddress.getLocalHost()));
  }

}
