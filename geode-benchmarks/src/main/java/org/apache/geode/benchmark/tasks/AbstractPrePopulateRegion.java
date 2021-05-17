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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.benchmark.topology.Roles;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;


public abstract class AbstractPrePopulateRegion<K, V> implements Task {
  private static final Logger logger = LoggerFactory.getLogger(AbstractPrePopulateRegion.class);

  private final LongRange keyRangeToPrepopulate;
  private final Roles targetRole;

  private int batchSize = 1000;

  public AbstractPrePopulateRegion() {
    this(new LongRange(0, 10000), CLIENT);
  }

  public AbstractPrePopulateRegion(LongRange keyRangeToPrepopulate) {
    this(keyRangeToPrepopulate, CLIENT);
  }

  public AbstractPrePopulateRegion(final LongRange keyRangeToPrepopulate, final Roles targetRole) {
    this.keyRangeToPrepopulate = keyRangeToPrepopulate;
    this.targetRole = targetRole;
  }

  /**
   * This method prepopulates the region before the actual benchmark starts.
   */
  @Override
  public void run(TestContext context) throws InterruptedException {
    final Cache cache = CacheFactory.getAnyInstance();
    final Region<K, V> region = cache.getRegion("region");
    final ArrayList<Integer> hostIds =
        new ArrayList<>(context.getHostsIDsForRole(targetRole.name()));

    run(region,
        keyRangeToPrepopulate.sliceFor(hostIds.size(), hostIds.indexOf(context.getJvmID())));
  }

  void run(final Map<K, V> region, final LongRange range) throws InterruptedException {
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

  private void doPuts(final Map<K, V> region, final LongRange range) {
    for (final LongRange slice : range.slicesOfSize(batchSize)) {
      final Map<K, V> valueMap = new HashMap<>();
      slice.forEach(i -> valueMap.put(getKey(i), getValue(i)));
      region.putAll(valueMap);
    }
  }

  protected abstract K getKey(long i);

  protected abstract V getValue(long i);

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }
}
