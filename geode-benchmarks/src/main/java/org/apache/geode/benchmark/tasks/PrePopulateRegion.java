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

import static org.apache.geode.benchmark.configurations.BenchmarkParameters.KEY_RANGE;
import static org.apache.geode.benchmark.configurations.BenchmarkParameters.SERVER_CACHE;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.data.PortfolioPdx;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;

public class PrePopulateRegion implements Task {
  long keyRangeToPrepopulate = KEY_RANGE;
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
  public void run(TestContext context) {
    Cache serverCache = (Cache) context.getAttribute(SERVER_CACHE);
    Region region = serverCache.getRegion("region");
    logger.info("*******************************************");
    logger.info("      Prepopulating the region ");
    logger.info("*******************************************");
    Instant start = Instant.now();
    LongStream.range(0, keyRangeToPrepopulate).forEach(i -> {
      long value = ThreadLocalRandom.current().nextLong(0, keyRangeToPrepopulate);
      region.put(i, new PortfolioPdx(value));
    });
    Instant finish = Instant.now();
    logger.info("*******************************************");
    logger.info("    Prepopulating the region completed");
    logger.info("    Duration = " + Duration.between(start, finish).toMillis() + "ms.");
    logger.info("*******************************************");
  }
}
