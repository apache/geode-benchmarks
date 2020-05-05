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

import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to start the SNI proxy
 */
public class StartSniProxy implements Task {

  public static final String START_DOCKER_DAEMON_COMMAND = "sudo service docker start";
  public static final String START_PROXY_COMMAND = "docker-compose up -d haproxy";

  private int locatorPort;

  public StartSniProxy(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {

    final String config = generateHaProxyConfig(
        hostNamesFor(context, LOCATOR),
        hostNamesFor(context, SERVER));

    writeToFile(config, "haproxy.cfg");

    final ProcessControl processControl = new ProcessControl();

    processControl.runCommand(START_DOCKER_DAEMON_COMMAND);
    processControl.runCommand(START_PROXY_COMMAND);
  }

  private void writeToFile(final String content, final String fileName) throws IOException {
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      writer.write(content);
    }
  }

  private List<String> hostNamesFor(final TestContext context, final String role) {
    return context.getHostsForRole(role).stream().map(InetAddress::getHostName)
        .collect(Collectors.toList());
  }

  String generateHaProxyConfig(final Iterable<String> locators,
      final Iterable<String> servers) {

    final StringBuilder stuff = new StringBuilder("defaults\n"
        + "  timeout client 1000\n"
        + "  timeout connect 1000\n"
        + "  timeout server 1000\n"
        + "frontend sniproxy\n"
        + "  bind *:15443\n"
        + "  mode tcp\n"
        + "  tcp-request inspect-delay 5s\n"
        + "  tcp-request content accept if { req_ssl_hello_type 1 }\n"
        + "  log stdout format raw  local0  debug\n");

    for (final String addy : locators) {
      stuff.append("  use_backend locators-").append(addy)
          .append(" if { req.ssl_sni -i ").append(addy).append(" }\n");
    }

    for (final String addy : servers) {
      stuff.append("  use_backend servers-").append(addy)
          .append(" if { req.ssl_sni -i ").append(addy).append(" }\n");
    }

    final String firstLocator = locators.iterator().next();
    stuff.append("  default_backend locators-").append(firstLocator).append("\n");

    for (final String addy : locators) {
      stuff.append("backend locators-").append(addy).append("\n")
          .append("  mode tcp\n").append("  server locator1 ").append(addy)
          .append(":" + locatorPort + "\n");
    }

    for (final String addy : servers) {
      stuff.append("backend servers-").append(addy).append("\n")
          .append("  mode tcp\n").append("  server server1 ").append(addy)
          .append(":40404\n");
    }
    return stuff.toString();
  }
}
