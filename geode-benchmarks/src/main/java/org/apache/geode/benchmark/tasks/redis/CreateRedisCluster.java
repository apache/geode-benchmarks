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
import static org.apache.geode.benchmark.tasks.ProcessControl.retryUntilZeroExit;
import static org.apache.geode.benchmark.tasks.ProcessControl.runAndExpectZeroExit;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to create the server cache and start the cache server.
 */
public class CreateRedisCluster implements Task {


  public CreateRedisCluster() {}

  @Override
  public void run(final TestContext context) throws Exception {
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

    retryUntilZeroExit(processBuilder);

    Thread.sleep(30000);
  }

}
