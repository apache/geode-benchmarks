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

import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.util.Set;
import java.util.stream.Collectors;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to create the client cache
 */
public class StartRedisClient implements Task {
  private static final Logger logger = LoggerFactory.getLogger(StartRedisClient.class);

  public StartRedisClient() {

  }

  @Override
  public void run(TestContext context) throws Exception {

    final Set<RedisURI> nodes = context.getHostsForRole(SERVER.name()).stream()
        .map(i -> RedisURI.create(i.getHostAddress(), 6379)).collect(Collectors.toSet());

    final RedisClusterClient redisClusterClient = RedisClusterClient.create(nodes);

    try (final StatefulRedisClusterConnection<String, String> connection = redisClusterClient.connect()) {
      while (true) {
        logger.info("Waiting for cluster to come up.");
        final String clusterInfo = connection.sync().clusterInfo();
        if (clusterInfo.contains("cluster_state:ok")) {
          break;
        }
        logger.debug(clusterInfo);
      }
    }

    RedisClusterClientSingleton.instance = redisClusterClient;
  }

}
