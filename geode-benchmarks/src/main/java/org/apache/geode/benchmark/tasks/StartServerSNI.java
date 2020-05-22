package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.getOffPlatformHostName;

import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.geode.cache.CacheFactory;
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
    cacheServer.setHostnameForClients(getOffPlatformHostName(context));
  }

}
