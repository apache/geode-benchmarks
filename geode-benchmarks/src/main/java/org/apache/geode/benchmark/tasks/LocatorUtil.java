package org.apache.geode.benchmark.tasks;

import java.net.InetAddress;
import java.util.Set;

import org.apache.geode.perftest.TestContext;

public class LocatorUtil {
  static String getLocatorString(TestContext context, int locatorPort) {
    Set<InetAddress> locators = context.getHostsForRole("locator");

    return locators.iterator().next().getHostAddress() + "[" + locatorPort + "]";
  }
}
