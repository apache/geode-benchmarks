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

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to start the SNI proxy
 */
public class StartSniProxy implements Task {
  public static final String START_DOCKER_DAEMON_COMMAND = "sudo service docker start";
  public static final String START_PROXY_COMMAND =
      "docker run --rm -d -v %s:/etc/envoy/envoy.yaml --name envoy -p %d:%d envoyproxy/envoy:v1.15-latest --log-level debug -c /etc/envoy/envoy.yaml";

  private final int locatorPort;
  private final int serverPort;
  private final int proxyPort;

  public StartSniProxy(final int locatorPort, final int serverPort, final int proxyPort) {
    this.locatorPort = locatorPort;
    this.serverPort = serverPort;
    this.proxyPort = proxyPort;
  }

  @Override
  public void run(final TestContext context) throws Exception {

    final Path configFile = Paths.get(getProperty("user.home"), "envoy.yaml");
    rewriteFile(generateEnvoyConfig(context), configFile);

    final ProcessControl processControl = new ProcessControl();
    processControl.runCommand(START_DOCKER_DAEMON_COMMAND);
    processControl.runCommand(format(START_PROXY_COMMAND, configFile, proxyPort, proxyPort));
  }

  private void rewriteFile(final String content, final Path path) throws IOException {
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), false))) {
      writer.write(content);
    }
  }


  String generateEnvoyConfig(final TestContext context) {

    StringBuilder yaml = new StringBuilder("static_resources:\n"
        + "  listeners:\n"
        + "    - name: geode_listener\n"
        + "      address:\n"
        + "        socket_address:\n"
        + "          address: 0.0.0.0\n"
        + "          port_value: ").append(proxyPort).append("\n"
            + "      listener_filters:\n"
            + "        - name: envoy.filters.listener.tls_inspector\n"
            + "      filter_chains:\n"
            + "        - filter_chain_match:\n"
            + "            server_names:\n");

    context.getHostsForRole(LOCATOR.name())
        .forEach(inetAddress -> yaml.append("              - '").append(inetAddress.getHostAddress())
            .append("'\n"));

    yaml.append("            transport_protocol: tls\n"
        + "          filters:\n"
        + "            - name: envoy.filters.network.sni_dynamic_forward_proxy\n"
        + "              typed_config:\n"
        + "                '@type': type.googleapis.com/envoy.extensions.filters.network.sni_dynamic_forward_proxy.v3alpha.FilterConfig\n"
        + "                port_value: ").append(locatorPort).append("\n"
            + "                dns_cache_config:\n"
            + "                  name: geode_cluster_dns_cache_config\n"
            + "                  dns_lookup_family: V4_ONLY\n"
            + "            - name: envoy.tcp_proxy\n"
            + "              typed_config:\n"
            + "                '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy\n"
            + "                stat_prefix: tcp\n"
            + "                cluster: geode_cluster\n"
            + "                access_log:\n"
            + "                  - name: envoy.access_loggers.file\n"
            + "                    typed_config:\n"
            + "                      \"@type\": type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog\n"
            + "                      log_format:\n"
            + "                        text_format: \"perf_listern: [%START_TIME%] %DOWNSTREAM_REMOTE_ADDRESS% %UPSTREAM_HOST% %RESPONSE_FLAGS% %BYTES_RECEIVED% %BYTES_SENT% %DURATION%\\n\"\n"
            + "                      path: /dev/stdout\n"
            + "        - filter_chain_match:\n"
            + "            server_names:\n");

    context.getHostsForRole(SERVER.name())
        .forEach(inetAddress -> yaml.append("              - '").append(inetAddress.getHostName())
            .append("'\n"));

    yaml.append("            transport_protocol: tls\n"
        + "          filters:\n"
        + "            - name: envoy.filters.network.sni_dynamic_forward_proxy\n"
        + "              typed_config:\n"
        + "                '@type': type.googleapis.com/envoy.extensions.filters.network.sni_dynamic_forward_proxy.v3alpha.FilterConfig\n"
        + "                port_value: ").append(serverPort).append("\n"
            + "                dns_cache_config:\n"
            + "                  name: geode_cluster_dns_cache_config\n"
            + "                  dns_lookup_family: V4_ONLY\n"
            + "            - name: envoy.tcp_proxy\n"
            + "              typed_config:\n"
            + "                '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy\n"
            + "                stat_prefix: tcp\n"
            + "                cluster: geode_cluster\n"
            + "                access_log:\n"
            + "                  - name: envoy.access_loggers.file\n"
            + "                    typed_config:\n"
            + "                      \"@type\": type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog\n"
            + "                      log_format:\n"
            + "                        text_format: \"perf_listern: [%START_TIME%] %DOWNSTREAM_REMOTE_ADDRESS% %UPSTREAM_HOST% %RESPONSE_FLAGS% %BYTES_RECEIVED% %BYTES_SENT% %DURATION%\\n\"\n"
            + "                      path: /dev/stdout\n"
            + "  clusters:\n"
            + "    - name: geode_cluster\n"
            + "      connect_timeout: 1s\n"
            + "      lb_policy: CLUSTER_PROVIDED\n"
            + "      cluster_type:\n"
            + "        name: envoy.clusters.dynamic_forward_proxy\n"
            + "        typed_config:\n"
            + "          '@type': type.googleapis.com/envoy.extensions.clusters.dynamic_forward_proxy.v3.ClusterConfig\n"
            + "          dns_cache_config:\n"
            + "            name: geode_cluster_dns_cache_config\n"
            + "            dns_lookup_family: V4_ONLY\n");
    return yaml.toString();
  }
}
