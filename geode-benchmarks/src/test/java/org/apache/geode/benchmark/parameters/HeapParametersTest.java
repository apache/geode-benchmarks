package org.apache.geode.benchmark.parameters;

import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.SERVER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestConfig;

class HeapParametersTest {

  private static final String WITH_HEAP = "withHeap";

  private Properties systemProperties;

  @BeforeEach
  public void beforeEach() {
    systemProperties = (Properties) System.getProperties().clone();
  }

  @AfterEach
  public void afterEach() {
    System.setProperties(systemProperties);
  }

  @Test
  public void withDefault() {
    System.clearProperty(WITH_HEAP);
    final TestConfig testConfig = new TestConfig();
    HeapParameters.configure(testConfig);
    assertHeap(testConfig, "8g");
  }

  @Test
  public void with16g() {
    System.setProperty(WITH_HEAP, "16g");
    final TestConfig testConfig = new TestConfig();
    HeapParameters.configure(testConfig);
    assertHeap(testConfig, "16g");
  }

  private void assertHeap(final TestConfig testConfig, final String heap) {
    assertThat(testConfig.getJvmArgs().get(CLIENT)).contains("-Xmx" + heap);
    assertThat(testConfig.getJvmArgs().get(SERVER)).contains("-Xmx" + heap);
    assertThat(testConfig.getJvmArgs().get(LOCATOR)).contains("-Xmx" + heap);
    assertThat(testConfig.getJvmArgs().get(CLIENT)).contains("-Xms" + heap);
    assertThat(testConfig.getJvmArgs().get(SERVER)).contains("-Xms" + heap);
    assertThat(testConfig.getJvmArgs().get(LOCATOR)).contains("-Xms" + heap);
  }
}