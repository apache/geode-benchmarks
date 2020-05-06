package org.apache.geode.benchmark;

import org.yardstickframework.BenchmarkDriver;

import org.apache.geode.benchmark.tasks.StopSniProxy;
import org.apache.geode.benchmark.topology.Roles;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestConfig;

/**
 * This is a more strongly-typed interface to the (stringly-typed) TestConfig interface in the
 * benchmark driver. This lets callers use the Roles enum instead of strings.
 */
public class Config {
  private Config() {}

  public static void role(final TestConfig config, final Roles role, final int numberOfInstances) {
    config.role(role.name(), numberOfInstances);
  }

  public static void before(final TestConfig config, final Task task, final Roles role) {
    config.before(task, role.name());
  }

  public static void workload(final TestConfig config, final BenchmarkDriver task, final Roles role) {
    config.workload(task, role.name());
  }

  public static void after(final TestConfig config, final StopSniProxy task, final Roles role) {
    config.after(task, role.name());
  }

  public static void jvmArgs(final TestConfig config, final Roles role, final String... jvmArgs) {
    config.jvmArgs(role.name(), jvmArgs);
  }
}
