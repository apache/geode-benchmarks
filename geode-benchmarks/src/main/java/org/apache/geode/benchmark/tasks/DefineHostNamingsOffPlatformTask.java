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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.geode.benchmark.topology.HostNamingOffPlatform;
import org.apache.geode.benchmark.topology.Roles;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class DefineHostNamingsOffPlatformTask implements Task {

  public static final String HOST_NAMINGS_OFF_PLATFORM = "HOST_NAMINGS_OFF_PLATFORM";

  public static String getOffPlatformHostName(final TestContext context,
      final InetAddress addy) throws UnknownHostException {
    final Map<InetAddress, String> namings =
        (Map<InetAddress, String>) context.getAttribute(HOST_NAMINGS_OFF_PLATFORM);
    return namings.get(addy);
  }

  @Override
  public void run(final TestContext context) throws Exception {
    final Map<InetAddress, String> namings =
        Stream.concat(
            generateHostNamingsFor(context, LOCATOR), generateHostNamingsFor(context, SERVER))
            .collect(
                Collectors.toMap(naming -> naming.internalAddy, naming -> naming.externalName));
    context.setAttribute(HOST_NAMINGS_OFF_PLATFORM, namings);
  }

  private Stream<HostNamingOffPlatform> generateHostNamingsFor(final TestContext context,
      final Roles role) {
    final AtomicInteger i = new AtomicInteger(0);
    final String roleName = role.name();
    return context.getHostsForRole(roleName).stream().map(host -> new HostNamingOffPlatform(
        formOffPlatformHostName(roleName, i.getAndIncrement()),
        host));
  }

  private String formOffPlatformHostName(final String roleName, final int i) {
    return roleName + "-OFF-PLATFORM-" + i;
  }

}
