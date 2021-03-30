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

package org.apache.geode.benchmark.tasks.redis;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.geode.benchmark.tasks.ProcessControl.retryUntilZeroExit;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class CreateRedisCluster implements Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateRedisCluster.class);

  @Override
  public void run(final TestContext context) throws Exception {
    final List<Integer> hostsIDsForRole =
        context.getHostsIDsForRole(CLIENT.name()).stream().sorted().collect(toList());
    final int self = context.getJvmID();
    final int position = hostsIDsForRole.indexOf(self);

    if (0 != position) {
      return;
    }

    final Set<InetAddress> servers = context.getHostsForRole(SERVER.name());

    final List<String> redisNodes =
        servers.stream().map(i -> i.getHostAddress() + ":6379").collect(Collectors.toList());

    final ProcessBuilder processBuilder =
        new ProcessBuilder().command("docker", "run", "--rm",
            "--network", "host",
            "bitnami/redis-cluster:latest",
            "redis-cli",
            "--cluster", "create");

    processBuilder.command().addAll(redisNodes);

    processBuilder.command().addAll(asList(
        "--cluster-replicas", "1",
        "--cluster-yes"));

    logger.info("Creating redis cluster. {}", processBuilder.command());

    retryUntilZeroExit(processBuilder);

    Thread.sleep(10_000);
  }

}
