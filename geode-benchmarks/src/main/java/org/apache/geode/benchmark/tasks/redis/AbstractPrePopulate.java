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

import static java.util.stream.Collectors.toList;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public abstract class AbstractPrePopulate implements Task {
  private static final Logger logger = LoggerFactory.getLogger(AbstractPrePopulate.class);

  private final RedisClientManager redisClientManager;
  private final LongRange keyRangeToPrepopulate;

  public AbstractPrePopulate(
      final RedisClientManager redisClientManager,
      final LongRange keyRangeToPrepopulate) {
    this.redisClientManager = redisClientManager;
    this.keyRangeToPrepopulate = keyRangeToPrepopulate;
  }

  @Override
  public void run(final TestContext context) throws Exception {
    final List<Integer> hostsIDsForRole =
        context.getHostsIDsForRole(CLIENT.name()).stream().sorted().collect(toList());
    final int self = context.getJvmID();
    final int position = hostsIDsForRole.indexOf(self);

    final LongRange keyRange = keyRangeToPrepopulate.sliceFor(hostsIDsForRole.size(), position);

    final int numThreads = Runtime.getRuntime().availableProcessors();
    final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
    final List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (final LongRange slice : keyRange.slice(numThreads)) {
      futures.add(CompletableFuture.runAsync(() -> {
        logger.info("Prepopulating slice: {} starting...", slice);
        final RedisClient redisClient = redisClientManager.get();
        slice.forEach(i -> {
          prepopulate(redisClient, i);
        });
      }, threadPool));
    }

    futures.forEach(CompletableFuture::join);

    threadPool.shutdownNow();
    threadPool.awaitTermination(5, TimeUnit.MINUTES);
  }

  protected abstract void prepopulate(final RedisClient redisClient, final long key);
}
