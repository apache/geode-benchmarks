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

import static org.apache.geode.benchmark.topology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import benchmark.geode.data.Portfolio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;


public class PrePopulateRegion implements Task {
  private static final Logger logger = LoggerFactory.getLogger(PrePopulateRegion.class);

  private LongRange keyRangeToPrepopulate = new LongRange(0, 10000);
  private int batchSize = 1000;

  public PrePopulateRegion() {}

  public PrePopulateRegion(LongRange keyRangeToPrepopulate) {
    this.keyRangeToPrepopulate = keyRangeToPrepopulate;
  }

  /**
   * This method prepopulates the region before the actual benchmark starts.
   */
  @Override
  public void run(TestContext context) throws InterruptedException {
    final ClientCache cache = ClientCacheFactory.getAnyInstance();
    final Region<Long, Portfolio> region = cache.getRegion("region");
    final int numLocators = context.getHostsIDsForRole(LOCATOR).size();
    final int numServers = context.getHostsIDsForRole(SERVER).size();
    final int numClient = context.getHostsIDsForRole(CLIENT).size();
    final int jvmID = context.getJvmID();
    final int clientIndex = jvmID - numLocators - numServers;

    run(region, keyRangeToPrepopulate.sliceFor(numClient, clientIndex));
  }

  void run(final Map<Long, Portfolio> region, final LongRange range) throws InterruptedException {
    logger.info("*******************************************");
    logger.info("      Prepopulating the region ");
    logger.info("*******************************************");
    final Instant start = Instant.now();

    final int numThreads = Runtime.getRuntime().availableProcessors();
    final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
    final List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (final LongRange slice : range.slice(numThreads)) {
      futures.add(CompletableFuture.runAsync(() -> doPuts(region, slice), threadPool));
    }

    futures.forEach(CompletableFuture::join);

    final Instant finish = Instant.now();
    logger.info("*******************************************");
    logger.info("    Prepopulating the region completed");
    logger.info("    Duration = " + Duration.between(start, finish).toMillis() + "ms.");
    logger.info("*******************************************");

    threadPool.shutdownNow();
    threadPool.awaitTermination(5, TimeUnit.MINUTES);
  }

  private void doPuts(final Map<Long, Portfolio> region, final LongRange range) {
    for (final LongRange slice : range.slicesOfSize(batchSize)) {
      final Map<Long, Portfolio> valueMap = new HashMap<>();
      slice.forEach(i -> valueMap.put(i, new Portfolio(i)));
      region.putAll(valueMap);
    }
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }
}
