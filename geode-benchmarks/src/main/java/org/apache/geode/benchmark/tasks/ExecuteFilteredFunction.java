/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.benchmark.tasks;

import java.io.Serializable;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import benchmark.geode.data.FunctionWithFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;

public class ExecuteFilteredFunction extends BenchmarkDriverAdapter implements Serializable {
  private Region region;
  long keyRange;
  long filterRange;
  private Function function;
  private static final Logger logger = LoggerFactory.getLogger(RemoteJVMFactory.class);

  public ExecuteFilteredFunction(long keyRange, long filterRange) {
    this.keyRange = keyRange;
    this.filterRange = filterRange;
    this.function = new FunctionWithFilter();
  }

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);
    ClientCache cache = ClientCacheFactory.getAnyInstance();
    region = cache.getRegion("region");
    FunctionService.registerFunction(function);
  }

  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    long minId = ThreadLocalRandom.current().nextLong(0, this.keyRange - filterRange);
    long maxId = minId + filterRange;
    Set<Long> filterSet = new HashSet<>();
    for (long i = minId; i <= maxId; i++) {
      filterSet.add(i);
    }
    ResultCollector resultCollector = FunctionService
        .onRegion(region)
        .withFilter(filterSet)
        .execute(function);
    List results = (List) resultCollector.getResult();
    validateResults(results, minId, maxId);
    return true;

  }

  private void validateResults(List results, long minId, long maxId)
      throws UnexpectedException {
    for (Object result : results) {
      ArrayList<Long> IDs = (ArrayList<Long>) result;
      for (Long id : IDs) {
        if (id < minId || id > maxId) {
          throw new UnexpectedException("Invalid ID value received [minID = " + minId + " maxID = "
              + maxId + " ] Portfolio ID received = " + id);
        }
      }
    }
  }
}
