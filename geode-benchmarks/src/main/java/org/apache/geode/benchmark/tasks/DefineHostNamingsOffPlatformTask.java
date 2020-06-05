package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.geode.benchmark.topology.HostNamingOffPlatform;
import org.apache.geode.benchmark.topology.Roles;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class DefineHostNamingsOffPlatformTask implements Task {

  public static final String HOST_NAMINGS_OFF_PLATFORM = "HOST_NAMINGS_OFF_PLATFORM";

  public static String getOffPlatformHostName(final TestContext context,
      final InetAddress addy) throws UnknownHostException {
    final Map<InetAddress, String> namings =
        (Map<InetAddress, String>) context.getAttribute(HOST_NAMINGS_OFF_PLATFORM);
    return namings.get(addy);
  }

  @Override
  public void run(final TestContext context) throws Exception {
    final Map<InetAddress, String> namings =
        Stream.concat(
            generateHostNamingsFor(context, LOCATOR), generateHostNamingsFor(context, SERVER))
            .collect(
                Collectors.toMap(naming -> naming.internalAddy, naming -> naming.externalName));
    context.setAttribute(HOST_NAMINGS_OFF_PLATFORM, namings);
  }

  private Stream<HostNamingOffPlatform> generateHostNamingsFor(final TestContext context,
      final Roles role) {
    final AtomicInteger i = new AtomicInteger(0);
    final String roleName = role.name();
    return context.getHostsForRole(roleName).stream().map(host -> new HostNamingOffPlatform(
        formOffPlatformHostName(roleName, i.getAndIncrement()),
        host));
  }

  private String formOffPlatformHostName(final String roleName, final int i) {
    return roleName + "-OFF-PLATFORM-" + i;
  }

}
