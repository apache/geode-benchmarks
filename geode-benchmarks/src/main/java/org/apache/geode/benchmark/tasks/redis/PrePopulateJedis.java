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

import static java.lang.String.valueOf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class PrePopulateJedis implements Task {
  private static final Logger logger = LoggerFactory.getLogger(PrePopulateJedis.class);

  private final LongRange keyRangeToPrepopulate;

  public PrePopulateJedis(final LongRange keyRangeToPrepopulate) {
    this.keyRangeToPrepopulate = keyRangeToPrepopulate;
  }

  @Override
  public void run(final TestContext context) throws Exception {
    final JedisCluster jedisCluster = JedisClusterSingleton.instance;

    final int numThreads = Runtime.getRuntime().availableProcessors();
    final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
    final List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (final LongRange slice : keyRangeToPrepopulate.slice(numThreads)) {
      futures.add(CompletableFuture.runAsync(() -> {
        logger.info("Prepopulating slice: {} starting...", slice);
          slice.forEach(i -> {
            final String key = valueOf(i);
            jedisCluster.set(key, key);
          });
        logger.info("Prepopulating slice: {} complete.", slice);
      }, threadPool));
    }

    futures.forEach(CompletableFuture::join);

    threadPool.shutdownNow();
    threadPool.awaitTermination(5, TimeUnit.MINUTES);
  }

}
