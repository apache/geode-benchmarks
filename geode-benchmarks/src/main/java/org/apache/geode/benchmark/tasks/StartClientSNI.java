package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.getOffPlatformHostName;
import static org.apache.geode.benchmark.topology.Roles.PROXY;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
                                                        final TestContext context)
      throws UnknownHostException {

    final InetAddress firstProxyAddy =
        context.getHostsForRole(PROXY.name()).iterator().next();
    final String
        offPlatformHostName = getOffPlatformHostName(context, firstProxyAddy);

    return super.createClientCacheFactory(locator, statsFile, properties, context)
        .setPoolSocketFactory(ProxySocketFactories.sni(
            offPlatformHostName,
            SNI_PROXY_PORT));
  }

}
