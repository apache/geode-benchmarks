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

import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.SERVER;

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

import org.apache.benchmark.geode.data.Portfolio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;

public class PrePopulateRegion implements Task {
  long keyRangeToPrepopulate = 10000;
  private int batchSize = 1000;
  private static final Logger logger = LoggerFactory.getLogger(RemoteJVMFactory.class);

  public PrePopulateRegion() {}

  public PrePopulateRegion(long keyRangeToPrepopulate) {
    this.keyRangeToPrepopulate = keyRangeToPrepopulate;
  }

  /**
   * This method prepopulates the region
   * before the actual benchmark starts.
   *
   */
  @Override
  public void run(TestContext context) throws InterruptedException {
    Cache serverCache = (Cache) context.getAttribute("SERVER_CACHE");
    Region<Long, Portfolio> region = serverCache.getRegion("region");
    int numLocators = context.getHostsForRole(LOCATOR).size();
    int numServers = context.getHostsForRole(SERVER).size();
    int jvmID = context.getJvmID();

    run(region, numLocators, numServers, jvmID);

  }

  void run(Map<Long, Portfolio> region, int numLocators, int numServers, int jvmID)
      throws InterruptedException {
    int serverIndex = jvmID - numLocators;
    long numPutsPerServer = this.keyRangeToPrepopulate / numServers;
    int numThreads =
        numPutsPerServer < getBatchSize() ? 1 : Runtime.getRuntime().availableProcessors();

    // calculate non-overlapping key ranges for each server
    long lowBound = numPutsPerServer * serverIndex;
    long highBound = numPutsPerServer * (serverIndex + 1);
    if (serverIndex == (numServers - 1)) {
      highBound += this.keyRangeToPrepopulate % (serverIndex + 1);
    }

    logger.info("*******************************************");
    logger.info("      Prepopulating the region ");
    logger.info("*******************************************");
    Instant start = Instant.now();

    ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    long range = highBound - lowBound;
    long putsPerThread = range / numThreads;

    for (int i = 0; i < numThreads; i++) {
      int threadNum = i;

      Runnable putThread = () -> {
        long low = lowBound + (putsPerThread * threadNum);
        long high = low + putsPerThread;

        if (threadNum == (numThreads - 1)) {
          high += range % numThreads;
        }

        doPuts(region, low, high);
      };

      futures.add(CompletableFuture.runAsync(putThread, threadPool));
    }

    futures.forEach(CompletableFuture::join);

    Instant finish = Instant.now();
    logger.info("*******************************************");
    logger.info("    Prepopulating the region completed");
    logger.info("    Duration = " + Duration.between(start, finish).toMillis() + "ms.");
    logger.info("*******************************************");

    threadPool.shutdownNow();
    threadPool.awaitTermination(5, TimeUnit.MINUTES);
  }

  private void doPuts(Map<Long, Portfolio> region, long lowBound, long highBound) {
    Map<Long, Portfolio> valueMap = new HashMap<>();
    for (long putIndex = lowBound; putIndex < highBound; putIndex++) {
      // build a map of to put to the server

      valueMap.put(putIndex, new Portfolio(putIndex));

      if (putIndex % getBatchSize() == 0) {
        region.putAll(valueMap);
        valueMap.clear();
      }
    }

    if (!valueMap.isEmpty()) {
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
