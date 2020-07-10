/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.benchmark.tests;

import static org.apache.geode.benchmark.topology.Ports.LOCATOR_PORT;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.PROXY;
import static org.assertj.core.api.Assertions.assertThat;

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

  public static final String WITH_CLUSTER_TOPOLOGY = "withClusterTopology";
  public static final String WITH_SNI_PROXY = "withSniProxy";
  private TestConfig config;
  private TestStep startProxyStep;

  @BeforeEach
  public void beforeEach() {
    System.clearProperty(WITH_SNI_PROXY);
    System.clearProperty(WITH_CLUSTER_TOPOLOGY);
    startProxyStep =
        new TestStep(new StartSniProxy(LOCATOR_PORT), new String[] {PROXY.name()});
  }

  @Test
  public void withoutSniProxy() {
    System.clearProperty(WITH_SNI_PROXY);
    config = GeodeBenchmark.createConfig();
    assertThat(config.getBefore()).doesNotContain(startProxyStep);
  }

  @Test
  public void withSniProxyFalse() {
    System.setProperty(WITH_SNI_PROXY, "false");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getBefore()).doesNotContain(startProxyStep);
  }

  @Test
  public void withSniProxyTrue() {
    System.setProperty(WITH_SNI_PROXY, "true");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getBefore()).contains(startProxyStep);
  }

  @Test
  public void withSniProxyNotLowercaseFalse() {
    System.setProperty(WITH_SNI_PROXY, "AnythING");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getBefore()).contains(startProxyStep);
  }

  @Test
  public void withClusterTopologyFalse() {
    System.setProperty(WITH_CLUSTER_TOPOLOGY, "false");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getRoles().get(CLIENT.name())).isNotNull();
  }

  @Test
  public void withClusterTopologyTrue() {
    System.setProperty(WITH_CLUSTER_TOPOLOGY, "true");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getRoles().get(CLIENT.name())).isNull();
  }

  @Test
  public void withClusterTopologyNotLowercaseFalse() {
    System.setProperty(WITH_CLUSTER_TOPOLOGY, "AnythING");
    config = GeodeBenchmark.createConfig();
    assertThat(config.getRoles().get(CLIENT.name())).isNull();
  }

}
