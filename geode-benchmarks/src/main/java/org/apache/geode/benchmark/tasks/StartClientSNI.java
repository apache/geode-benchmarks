package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.topology.Roles.PROXY;

import java.net.InetAddress;
import java.util.Properties;

import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.proxy.ProxySocketFactories;
import org.apache.geode.perftest.TestContext;

public class StartClientSNI extends StartClient {
  public static final int SNI_PROXY_PORT = 15443;

  public StartClientSNI(final int locatorPort) {
    super(locatorPort);
  }

  @Override
  protected ClientCacheFactory createClientCacheFactory(final InetAddress locator,
                                                        final String statsFile,
                                                        final Properties properties,
                                                        final TestContext context) {

    final InetAddress proxyAddy = context.getHostsForRole(PROXY.name()).iterator().next();

    return super.createClientCacheFactory(locator, statsFile, properties, context)
        .setPoolSocketFactory(ProxySocketFactories.sni(
            proxyAddy.getHostName(),
            SNI_PROXY_PORT));
  }

}
