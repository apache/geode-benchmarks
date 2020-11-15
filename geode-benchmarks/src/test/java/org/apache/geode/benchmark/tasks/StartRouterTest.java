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

class StartRouterTest {

  @Test
  void generateConfig() {
    final Set<InetSocketAddress> members = new HashSet<InetSocketAddress>() {
      {
        add(InetSocketAddress.createUnresolved("p1", 1));
        add(InetSocketAddress.createUnresolved("p2", 2));
      }
    };
    final StartRouter startRouter = new StartRouter(3, null);
    final String config = startRouter.generateConfig(members);

    assertThat(config).isEqualTo("global\n"
        + "  daemon\n"
        + "  maxconn 64000\n"
        + "  spread-checks 4\n"
        + "defaults\n"
        + "  log global\n"
        + "  timeout connect 30000ms\n"
        + "  timeout client 30000ms\n"
        + "  timeout server 30000ms\n"
        + "listen router\n"
        + "  bind *:3\n"
        + "  mode tcp\n"
        + "  server p1 p1:1\n"
        + "  server p2 p2:2\n");
  }

  @Test
  public void equals() {
    assertThat(new StartRouter(3, null)).isEqualTo(new StartRouter(3, null));
    assertThat(new StartRouter(3, "a")).isEqualTo(new StartRouter(3, "a"));
    assertThat(new StartRouter(3, "a")).isNotEqualTo(new StartRouter(3, "b"));
  }

  @Test
  public void hashcode() {
    assertThat(new StartRouter(3, null).hashCode()).isEqualTo(new StartRouter(3, null).hashCode());
    assertThat(new StartRouter(3, "a").hashCode()).isEqualTo(new StartRouter(3, "a").hashCode());
    assertThat(new StartRouter(3, "a").hashCode()).isNotEqualTo(new StartRouter(3, "b").hashCode());
  }

}
