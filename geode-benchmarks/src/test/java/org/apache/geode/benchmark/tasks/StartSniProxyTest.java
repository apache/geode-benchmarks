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
package org.apache.geode.benchmark.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class StartSniProxyTest {

  @Test
  public void generateConfigTest() {
    final StartSniProxy starter = new StartSniProxy(42);
    final String config =
        starter.generateHaProxyConfig(Stream.of("locator-one-internal"),
            Stream.of("locator-one-external"),
            Stream.of("server-one-internal", "server-two-internal"),
            Stream.of("server-one-external", "server-two-external"));
    assertThat(config).isEqualTo("global\n"
        + "  log stdout format raw local0 debug\n"
        + "  maxconn 5000\n"
        + "  nbproc 2\n"
        + "  nbthread 64\n"
        + "defaults\n"
        + "  log global\n"
        /*
         * We're leaving timeouts unspecified so they are infinite. Benchmarks do bad things
         * when the proxy breaks connections.
         */
        // + " timeout client 100s\n"
        // + " timeout connect 100s\n"
        // + " timeout server 100s\n"
        + "frontend sniproxy\n"
        + "  bind *:15443\n"
        + "  mode tcp\n"
        + "  tcp-request inspect-delay 5s\n"
        + "  tcp-request content accept if { req_ssl_hello_type 1 }\n"
        + "  use_backend locators-locator-one-internal if { req.ssl_sni -i locator-one-external }\n"
        + "  use_backend servers-server-one-internal if { req.ssl_sni -i server-one-external }\n"
        + "  use_backend servers-server-two-internal if { req.ssl_sni -i server-two-external }\n"
        + "  default_backend locators-locator-one-internal\n"
        + "backend locators-locator-one-internal\n"
        + "  mode tcp\n"
        + "  server locator1 locator-one-internal:42\n"
        + "backend servers-server-one-internal\n"
        + "  mode tcp\n"
        + "  server server1 server-one-internal:40404\n"
        + "backend servers-server-two-internal\n"
        + "  mode tcp\n"
        + "  server server1 server-two-internal:40404\n");
  }
}
