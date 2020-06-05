package org.apache.geode.benchmark.tests;

import static org.apache.geode.benchmark.topology.Ports.LOCATOR_PORT;
import static org.apache.geode.benchmark.topology.Roles.PROXY;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.geode.benchmark.tasks.StartSniProxy;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestStep;

/**
 * Verify that if withSniProxy system property is set at all and if it is set to anything but
 * false, that we get an SNI proxy in our topology. If the system property is not set at all, or
 * if it is set to (exactly) "false" then we do not get an SNI proxy in our topology.
 */
class GeodeBenchmarkTest {

  private TestConfig config;
  private TestStep startProxyStep;

  @BeforeEach
  public void beforeEach() {
    startProxyStep =
        new TestStep(new StartSniProxy(LOCATOR_PORT), new String[] {PROXY.name()});
  }

  @AfterAll
  public static void afterAll() {
    System.clearProperty("withSniProxy");
  }

  @Test
  public void withoutSniProxy() {
    System.clearProperty("withSniProxy");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getBefore()).doesNotContain(startProxyStep);
  }

  @Test
  public void withSniProxyFalse() {
    System.setProperty("withSniProxy", "false");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getBefore()).doesNotContain(startProxyStep);
  }

  @Test
  public void withSniProxyTrue() {
    System.setProperty("withSniProxy", "true");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getBefore()).contains(startProxyStep);
  }

  @Test
  public void withSniProxyNotLowercaseFalse() {
    System.setProperty("withSniProxy", "AnythING");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getBefore()).contains(startProxyStep);
  }

}
