package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.net.InetAddress;
import java.util.List;
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

  @Override
  public void run(final TestContext context) throws Exception {
    final Map<InetAddress, String>
        namings =
        Stream.concat(hostNamingsFor(context, LOCATOR), hostNamingsFor(context, SERVER))
            .collect(
                Collectors.toMap(naming -> naming.internalAddy, naming -> naming.externalName));
    context.setAttribute(HOST_NAMINGS_OFF_PLATFORM, namings);
  }

  private Stream<HostNamingOffPlatform> hostNamingsFor(final TestContext context, final Roles role) {
    final AtomicInteger i = new AtomicInteger(0);
    return context.getHostsForRole(role.name()).stream().map(host ->
        new HostNamingOffPlatform(
            role.name() + "-" + i.getAndIncrement(),
            host));
  }

}
