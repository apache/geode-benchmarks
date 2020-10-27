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
import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.HOST_NAMINGS_OFF_PLATFORM;
import static org.apache.geode.benchmark.topology.Ports.SERVER_PORT_FOR_SNI;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.geode.benchmark.topology.Roles;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to start the SNI proxy
 */
public class StartSniProxy implements Task {
  public static final String START_DOCKER_DAEMON_COMMAND = "sudo service docker start";
  public static final String START_PROXY_COMMAND = "docker run --rm -d -v %s/envoy.yaml:/etc/envoy/envoy.yaml --name envoy -p 15443:15443 envoyproxy/envoy:v1.15-latest";

  private final int locatorPort;

  public StartSniProxy(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    @SuppressWarnings("unchecked")
    final Map<InetAddress, String> namings =
        (Map<InetAddress, String>) context.getAttribute(HOST_NAMINGS_OFF_PLATFORM);

    final String config = generateEnvoyConfig(
        internalHostNamesFor(context, LOCATOR),
        externalHostNamesFor(context, LOCATOR, namings),
        internalHostNamesFor(context, SERVER),
        externalHostNamesFor(context, SERVER, namings));

    rewriteFile(config, "envoy.yaml");

    final ProcessControl processControl = new ProcessControl();
    processControl.runCommand(START_DOCKER_DAEMON_COMMAND);
    processControl.runCommand(format(START_PROXY_COMMAND, getProperty("user.home")));
  }

  private void rewriteFile(final String content, final String fileName) throws IOException {
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
      writer.write(content);
    }
  }

  private Stream<String> internalHostNamesFor(final TestContext context, final Roles role) {
    return addysFor(context, role).map(InetAddress::getHostName);
  }

  private Stream<String> externalHostNamesFor(final TestContext context, final Roles role,
      final Map<InetAddress, String> namings) {
    return addysFor(context, role).map(addy -> namings.get(addy));
  }

  private Stream<InetAddress> addysFor(final TestContext context, final Roles role) {
    return context.getHostsForRole(role.name()).stream();
  }

  String generateEnvoyConfig(final Stream<String> locatorsInternalStream,
                               final Stream<String> locatorsExternalStream,
                               final Stream<String> serversInternalStream,
                               final Stream<String> serversExternalStream) {

    StringBuilder yaml = new StringBuilder("static_resources:\n"
        + "  listeners:\n"
        + "    - name: geode_listener\n"
        + "      address:\n"
        + "        socket_address:\n"
        + "          address: 0.0.0.0\n"
        + "          port_value: 15443\n"
        + "      listener_filters:\n"
        + "        - name: envoy.filters.listener.tls_inspector\n"
        + "      filter_chains:\n"
        + "        - filter_chain_match:\n"
        + "            server_names:\n");

    locatorsInternalStream.forEach(hostname -> {
      yaml.append("              - '").append(hostname).append("'\n");
    });

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
        + "                        text_format: \"[%START_TIME%] %DOWNSTREAM_REMOTE_ADDRESS% %UPSTREAM_HOST% %RESPONSE_FLAGS% %BYTES_RECEIVED% %BYTES_SENT% %DURATION%\\n\"\n"
        + "                      path: /dev/stdout\n"
        + "        - filter_chain_match:\n"
        + "            server_names:\n");

    serversInternalStream.forEach(hostname -> {
      yaml.append("              - '").append(hostname).append("'\n");
    });

    yaml.append("            transport_protocol: tls\n"
        + "          filters:\n"
        + "            - name: envoy.filters.network.sni_dynamic_forward_proxy\n"
        + "              typed_config:\n"
        + "                '@type': type.googleapis.com/envoy.extensions.filters.network.sni_dynamic_forward_proxy.v3alpha.FilterConfig\n"
        + "                port_value: ").append(SERVER_PORT_FOR_SNI).append("\n"
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
        + "                        text_format: \"[%START_TIME%] %DOWNSTREAM_REMOTE_ADDRESS% %UPSTREAM_HOST% %RESPONSE_FLAGS% %BYTES_RECEIVED% %BYTES_SENT% %DURATION%\\n\"\n"
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

    String generateHaProxyConfig(final Stream<String> locatorsInternalStream,
      final Stream<String> locatorsExternalStream,
      final Stream<String> serversInternalStream,
      final Stream<String> serversExternalStream) {

    final Iterable<String> locatorsInternal = locatorsInternalStream.collect(Collectors.toList());
    final Iterable<String> locatorsExternal = locatorsExternalStream.collect(Collectors.toList());
    final Iterable<String> serversInternal = serversInternalStream.collect(Collectors.toList());
    final Iterable<String> serversExternal = serversExternalStream.collect(Collectors.toList());

    final StringBuilder stuff = new StringBuilder(
        /*
         * log to stdout per:
         * https://www.haproxy.com/documentation/hapee/latest/administration/docker-logging/
         */
        "global\n"
            + "  log stdout format raw local0 debug\n"
            + "  maxconn 5000\n"
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
            + "  tcp-request content accept if { req_ssl_hello_type 1 }\n");

    generateUseBackendRule(locatorsInternal, locatorsExternal, stuff, "locators-");
    generateUseBackendRule(serversInternal, serversExternal, stuff, "servers-");

    final String firstLocatorInternal = locatorsInternal.iterator().next();
    stuff.append("  default_backend ").append("locators-").append(firstLocatorInternal)
        .append("\n");

    generateBackendSection(locatorsInternal, stuff, "locators-",
        "locator1", locatorPort);

    generateBackendSection(serversInternal, stuff, "servers-",
        "server1", SERVER_PORT_FOR_SNI);

    return stuff.toString();
  }

  private void generateUseBackendRule(final Iterable<String> internalsIterable,
      final Iterable<String> externalsIterable,
      final StringBuilder stuff,
      final String backendNamePrefix) {
    final Iterator<String> internals = internalsIterable.iterator();
    final Iterator<String> externals = externalsIterable.iterator();
    while (internals.hasNext() && externals.hasNext()) {
      final String internal = internals.next();
      final String external = externals.next();
      stuff.append("  use_backend ").append(backendNamePrefix).append(internal)
          .append(" if { req.ssl_sni -i ").append(external).append(" }\n");
    }
  }

  private void generateBackendSection(final Iterable<String> internalsIterator,
      final StringBuilder stuff,
      final String backendNamePrefix,
      final String singleHostRoleName,
      final int port) {
    for (final String addy : internalsIterator) {
      stuff.append("backend ").append(backendNamePrefix).append(addy).append("\n")
          .append("  mode tcp\n").append("  server ").append(singleHostRoleName).append(" ")
          .append(addy)
          .append(":").append(port).append("\n");
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final StartSniProxy that = (StartSniProxy) o;
    return locatorPort == that.locatorPort;
  }

  @Override
  public int hashCode() {
    return Objects.hash(locatorPort);
  }
}
