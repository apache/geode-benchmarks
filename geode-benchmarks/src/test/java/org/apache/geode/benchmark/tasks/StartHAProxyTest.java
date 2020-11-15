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

package org.apache.geode.benchmark.tasks;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class StartHAProxyTest {

  @Test
  void generateConfig() {
    final Set<InetSocketAddress> members = new HashSet<InetSocketAddress>() {
      {
        add(InetSocketAddress.createUnresolved("l1", 1));
        add(InetSocketAddress.createUnresolved("s2", 2));
        add(InetSocketAddress.createUnresolved("s3", 3));
      }
    };
    final StartHAProxy startHAProxy = new StartHAProxy(0, 0, 3);
    final String config = startHAProxy.generateConfig(members);

    assertThat(config).isEqualTo("global\n"
        + "  maxconn 5000\n"
        + "defaults\n"
        + "  log global\n"
        + "frontend sniproxy\n"
        + "  bind *:3\n"
        + "  mode tcp\n"
        + "  tcp-request inspect-delay 5s\n"
        + "  tcp-request content accept if { req_ssl_hello_type 1 }\n"
        + "  use_backend s2 if { req.ssl_sni -i s2 }\n"
        + "  use_backend s3 if { req.ssl_sni -i s3 }\n"
        + "  use_backend l1 if { req.ssl_sni -i l1 }\n"
        + "backend s2\n"
        + "  mode tcp\n"
        + "  server host s2:2\n"
        + "backend s3\n"
        + "  mode tcp\n"
        + "  server host s3:3\n"
        + "backend l1\n"
        + "  mode tcp\n"
        + "  server host l1:1\n");
  }
}
