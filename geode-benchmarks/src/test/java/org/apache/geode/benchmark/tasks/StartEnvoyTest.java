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

import static java.net.InetAddress.getByAddress;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.TestContext;

class StartEnvoyTest {

  @Test
  void generateConfig() throws UnknownHostException {
    final TestContext testContext = mock(TestContext.class);
    final Set<InetAddress> locators =
        Collections.singleton(getByAddress("l1", new byte[] {0, 0, 0, 0}));
    when(testContext.getHostsForRole(LOCATOR.name()))
        .thenReturn(locators);
    final HashSet<InetAddress> servers = new HashSet<InetAddress>() {
      {
        add(getByAddress("s1", new byte[] {0, 0, 0, 1}));
        add(getByAddress("s2", new byte[] {0, 0, 0, 2}));
      }
    };
    when(testContext.getHostsForRole(SERVER.name())).thenReturn(servers);

    final StartEnvoy startEnvoy = new StartEnvoy(0, 0, 3, null);
    final String config = startEnvoy.generateConfig(testContext);

    assertThat(config).isEqualTo("static_resources:\n"
        + "  listeners:\n"
        + "    - name: geode_listener\n"
        + "      address:\n"
        + "        socket_address:\n"
        + "          address: 0.0.0.0\n"
        + "          port_value: 3\n"
        + "      reuse_port: true\n"
        + "      tcp_backlog_size: 1000\n"
        + "      listener_filters:\n"
        + "        - name: envoy.filters.listener.tls_inspector\n"
        + "      filter_chains:\n"
        + "        - filter_chain_match:\n"
        + "            server_names:\n"
        + "              - 'l1'\n"
        + "            transport_protocol: tls\n"
        + "          filters:\n"
        + "            - name: envoy.filters.network.sni_dynamic_forward_proxy\n"
        + "              typed_config:\n"
        + "                '@type': type.googleapis.com/envoy.extensions.filters.network.sni_dynamic_forward_proxy.v3alpha.FilterConfig\n"
        + "                port_value: 0\n"
        + "                dns_cache_config:\n"
        + "                  name: geode_cluster_dns_cache_config\n"
        + "                  dns_lookup_family: V4_ONLY\n"
        + "            - name: envoy.tcp_proxy\n"
        + "              typed_config:\n"
        + "                '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy\n"
        + "                stat_prefix: tcp\n"
        + "                cluster: geode_cluster\n"
        + "                max_connect_attempts: 1000000000\n"
        + "                access_log:\n"
        + "                  - name: envoy.access_loggers.file\n"
        + "                    typed_config:\n"
        + "                      \"@type\": type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog\n"
        + "                      log_format:\n"
        + "                        text_format: \"[%START_TIME%] %DOWNSTREAM_REMOTE_ADDRESS% %UPSTREAM_HOST% %RESPONSE_FLAGS% %BYTES_RECEIVED% %BYTES_SENT% %DURATION%\\n\"\n"
        + "                      path: /dev/stdout\n"
        + "        - filter_chain_match:\n"
        + "            server_names:\n"
        + "              - 's1'\n"
        + "              - 's2'\n"
        + "            transport_protocol: tls\n"
        + "          filters:\n"
        + "            - name: envoy.filters.network.sni_dynamic_forward_proxy\n"
        + "              typed_config:\n"
        + "                '@type': type.googleapis.com/envoy.extensions.filters.network.sni_dynamic_forward_proxy.v3alpha.FilterConfig\n"
        + "                port_value: 0\n"
        + "                dns_cache_config:\n"
        + "                  name: geode_cluster_dns_cache_config\n"
        + "                  dns_lookup_family: V4_ONLY\n"
        + "            - name: envoy.tcp_proxy\n"
        + "              typed_config:\n"
        + "                '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy\n"
        + "                stat_prefix: tcp\n"
        + "                cluster: geode_cluster\n"
        + "                max_connect_attempts: 1000000000\n"
        + "                access_log:\n"
        + "                  - name: envoy.access_loggers.file\n"
        + "                    typed_config:\n"
        + "                      \"@type\": type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog\n"
        + "                      log_format:\n"
        + "                        text_format: \"[%START_TIME%] %DOWNSTREAM_REMOTE_ADDRESS% %UPSTREAM_HOST% %RESPONSE_FLAGS% %BYTES_RECEIVED% %BYTES_SENT% %DURATION%\\n\"\n"
        + "                      path: /dev/stdout\n"
        + "  clusters:\n"
        + "    - name: geode_cluster\n"
        + "      connect_timeout: 10s\n"
        + "      lb_policy: CLUSTER_PROVIDED\n"
        + "      cluster_type:\n"
        + "        name: envoy.clusters.dynamic_forward_proxy\n"
        + "        typed_config:\n"
        + "          '@type': type.googleapis.com/envoy.extensions.clusters.dynamic_forward_proxy.v3.ClusterConfig\n"
        + "          dns_cache_config:\n"
        + "            name: geode_cluster_dns_cache_config\n"
        + "            dns_lookup_family: V4_ONLY\n");
  }

  @Test
  public void equals() {
    assertThat(new StartEnvoy(1, 2, 3, null)).isEqualTo(new StartEnvoy(1, 2, 3, null));
    assertThat(new StartEnvoy(1, 2, 3, "a")).isEqualTo(new StartEnvoy(1, 2, 3, "a"));
    assertThat(new StartEnvoy(1, 2, 3, "a")).isNotEqualTo(new StartEnvoy(1, 2, 3, "b"));
  }

  @Test
  public void hashcode() {
    assertThat(new StartEnvoy(1, 2, 3, null).hashCode())
        .isEqualTo(new StartEnvoy(1, 2, 3, null).hashCode());
    assertThat(new StartEnvoy(1, 2, 3, "a").hashCode())
        .isEqualTo(new StartEnvoy(1, 2, 3, "a").hashCode());
    assertThat(new StartEnvoy(1, 2, 3, "a").hashCode())
        .isNotEqualTo(new StartEnvoy(1, 2, 3, "b").hashCode());
  }

}
