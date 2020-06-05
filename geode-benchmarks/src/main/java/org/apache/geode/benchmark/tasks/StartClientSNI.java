package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.getOffPlatformHostName;
import static org.apache.geode.benchmark.topology.Ports.SNI_PROXY_PORT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.PROXY;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.proxy.ProxySocketFactories;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.apache.geode.perftest.TestContext;

public class StartClientSNI extends StartClient {

  public StartClientSNI(final int locatorPort) {
    super(locatorPort);
  }

  @Override
  protected ClientCacheFactory createClientCacheFactory(final InetAddress locator,
      final String statsFile,
      final Properties properties,
      final TestContext context)
      throws UnknownHostException {

    final InetAddress firstLocatorAddy =
        context.getHostsForRole(LOCATOR.name()).iterator().next();
    final String offPlatformLocatorName =
        getOffPlatformHostName(context, firstLocatorAddy);
    final InetAddress proxyAddy =
        context.getHostsForRole(PROXY.name()).iterator().next();

    return new ClientCacheFactory(properties)
        .setPdxSerializer(new ReflectionBasedAutoSerializer("benchmark.geode.data.*"))
        .setPoolIdleTimeout(-1)
        .set(ConfigurationProperties.STATISTIC_ARCHIVE_FILE, statsFile)
        .addPoolLocator(offPlatformLocatorName, locatorPort)
        .setPoolSocketFactory(ProxySocketFactories.sni(
            proxyAddy.getHostAddress(),
            SNI_PROXY_PORT));
  }

}
