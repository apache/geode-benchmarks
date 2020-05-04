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

import java.util.Collections;

import org.junit.jupiter.api.Test;

class StartSniProxyTest {

  @Test
  public void generateConfigTest() {
    final StartSniProxy starter = new StartSniProxy(42);
    final String config =
        starter.generateHaProxyConfig(Collections.singleton("one"), Collections.singleton("two"));
    assertThat(config).isEqualTo("defaults\n"
        + "  timeout client 1000\n"
        + "  timeout connect 1000\n"
        + "  timeout server 1000\n"
        + "frontend sniproxy\n"
        + "  bind *:15443\n"
        + "  mode tcp\n"
        + "  tcp-request inspect-delay 5s\n"
        + "  tcp-request content accept if { req_ssl_hello_type 1 }\n"
        + "  log stdout format raw  local0  debug\n"
        + "  use_backend locators-one if { req.ssl_sni -i one }\n"
        + "  use_backend servers-two if { req.ssl_sni -i two }\n"
        + "  default_backend locators-one\n"
        + "backend locators-one\n"
        + "  mode tcp\n"
        + "  server locator1 one:42\n"
        + "backend servers-two\n"
        + "  mode tcp\n"
        + "  server server1 two:40404\n");
  }
}
