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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to start the SNI proxy
 */
public class StartHAProxy implements Task {
  public static final String START_DOCKER_DAEMON_COMMAND = "sudo service docker start";
  public static final String START_PROXY_COMMAND =
      "docker run --rm -d -v %s:/usr/local/etc/haproxy:ro --name proxy -p %d:%d %s";

  private final int locatorPort;
  private final int serverPort;
  private final int proxyPort;
  private final String image;

  public StartHAProxy(final int locatorPort, final int serverPort, final int proxyPort,
      final String image) {
    this.locatorPort = locatorPort;
    this.serverPort = serverPort;
    this.proxyPort = proxyPort;
    this.image = isNullOrEmpty(image) ? "haproxy:1.8-alpine" : image;
  }

  @Override
  public void run(final TestContext context) throws Exception {

    final Path configPath = Paths.get(getProperty("user.home")).toAbsolutePath();
    final Path configFile = configPath.resolve("haproxy.cfg");
    rewriteFile(generateConfig(context), configFile);

    final ProcessControl processControl = new ProcessControl();
    processControl.runCommand(START_DOCKER_DAEMON_COMMAND);
    processControl.runCommand(format(START_PROXY_COMMAND, configPath, proxyPort, proxyPort, image));
  }

  private void rewriteFile(final String content, final Path path) throws IOException {
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), false))) {
      writer.write(content);
    }
  }

  String generateConfig(final TestContext context) {

    final Set<InetSocketAddress> members = new HashSet<>();

    context.getHostsForRole(LOCATOR.name()).stream()
        .map(a -> InetSocketAddress.createUnresolved(a.getHostName(), locatorPort))
        .forEachOrdered(members::add);

    context.getHostsForRole(SERVER.name()).stream()
        .map(a -> InetSocketAddress.createUnresolved(a.getHostName(), serverPort))
        .forEachOrdered(members::add);

    return generateConfig(members);
  }

  String generateConfig(final Set<InetSocketAddress> members) {
    StringBuilder conf = new StringBuilder("global\n"
        + "  daemon\n"
        + "  maxconn 64000\n"
        + "  spread-checks 4\n"
        + "defaults\n"
        + "  log global\n"
        + "  timeout connect 30000ms\n"
        + "  timeout client 30000ms\n"
        + "  timeout server 30000ms\n"
        + "frontend sniproxy\n"
        + "  bind *:").append(proxyPort).append("\n"
            + "  mode tcp\n"
            + "  tcp-request inspect-delay 5s\n"
            + "  tcp-request content accept if { req_ssl_hello_type 1 }\n");

    members
        .forEach(s -> conf.append("  use_backend ").append(s.getHostName())
            .append(" if { req.ssl_sni ").append(s.getHostName()).append(" }\n"));

    members
        .forEach(s -> conf.append("backend ").append(s.getHostName())
            .append("\n"
                + "  mode tcp\n"
                + "  server host ")
            .append(s.getHostName())
            .append(":").append(s.getPort()).append("\n"));

    return conf.toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final StartHAProxy that = (StartHAProxy) o;
    return locatorPort == that.locatorPort &&
        serverPort == that.serverPort &&
        proxyPort == that.proxyPort &&
        Objects.equals(image, that.image);
  }

  @Override
  public int hashCode() {
    return Objects.hash(locatorPort, serverPort, proxyPort, image);
  }
}
