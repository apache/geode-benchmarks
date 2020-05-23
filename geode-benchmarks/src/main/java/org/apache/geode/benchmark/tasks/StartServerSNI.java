package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.getOffPlatformHostName;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.perftest.TestContext;

public class StartServerSNI extends StartServer {

  private final int serverPortForSni;

  public StartServerSNI(final int locatorPort, final int serverPortForSni) {
    super(locatorPort);
    this.serverPortForSni = serverPortForSni;
  }

  @Override
  protected void configureCacheServer(final CacheServer cacheServer, final TestContext context)
      throws UnknownHostException {
    cacheServer.setMaxConnections(Integer.MAX_VALUE);
    cacheServer.setPort(serverPortForSni);
    cacheServer.setHostnameForClients(
        getOffPlatformHostName(context, InetAddress.getLocalHost()));
  }

}
