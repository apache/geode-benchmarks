/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.benchmark.topology;

import static java.lang.System.setProperty;
import static org.apache.geode.benchmark.topology.ClientServerTopologyWithSniProxy.WITH_SNI_PROXY_PROPERTY;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

import org.apache.geode.benchmark.topology.ClientServerTopologyWithSniProxy.SniProxyImplementation;
import org.apache.geode.perftest.TestConfig;

public class ClientServerTopologyWithSniProxyTest {

  @ParameterizedTest
  @EnumSource(SniProxyImplementation.class)
  @ClearSystemProperty(key = WITH_SNI_PROXY_PROPERTY)
  public void configWithNoSsl(final SniProxyImplementation sniProxyImplementation) {
    setProperty(WITH_SNI_PROXY_PROPERTY, sniProxyImplementation.name());
    final TestConfig testConfig = new TestConfig();
    ClientServerTopologyWithSniProxy.configure(testConfig);
    assertThat(testConfig.getJvmArgs().get(CLIENT.name())).contains("-Dbenchmark.withSsl=true");
  }

}
