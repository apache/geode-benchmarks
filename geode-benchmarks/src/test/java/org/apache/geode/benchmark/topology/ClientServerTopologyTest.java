package org.apache.geode.benchmark.topology;


import static org.apache.geode.benchmark.parameters.JVMParameters.JVM8_ARGS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestConfig;

public class ClientServerTopologyTest {


  @BeforeEach
  @AfterEach
  public void clearProperties() {
    System.clearProperty("withSsl");
  }

  @Test
  public void configWithSsl() {
    System.setProperty("withSsl", "true");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).contains("-DwithSsl");
  }

  @Test
  public void configWithNoSsl() {
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain("-DwithSsl");
  }

  @Test
  public void configWithJava8() {
    System.setProperty("java.runtime.version", "1.8.0_212");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain("-DwithSsl");
    assertThat(testConfig.getJvmArgs().get("client")).contains(JVM8_ARGS);
  }
 
  @Test
  public void configWithJava9OrHigher() {
    System.setProperty("java.runtime.version", "9.0.1");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain("-DwithSsl");
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain(JVM8_ARGS);
  }

  @Test
  public void configWithSslAndJava8() {
    System.setProperty("withSsl", "true");
    System.setProperty("java.runtime.version", "1.8.0_212");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).contains("-DwithSsl");
    assertThat(testConfig.getJvmArgs().get("client")).contains(JVM8_ARGS);
  }

  @Test
  public void configWithSslAndJava9() {
    System.setProperty("withSsl", "true");
    System.setProperty("java.runtime.version", "9.0.1");
    TestConfig testConfig = new TestConfig();
    ClientServerTopology.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get("client")).contains("-DwithSsl");
    assertThat(testConfig.getJvmArgs().get("client")).doesNotContain(JVM8_ARGS);
  }
}