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
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import benchmark.geode.data.Portfolio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.query.FunctionDomainException;
import org.apache.geode.cache.query.NameResolutionException;
import org.apache.geode.cache.query.QueryInvocationTargetException;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.TypeMismatchException;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;

public class OQLQuery extends BenchmarkDriverAdapter implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(RemoteJVMFactory.class);
  private Region<Object, Object> region;
  private long keyRange;
  private long queryRange;
  ClientCache cache;

  public OQLQuery(long keyRange, long queryRange) {
    this.keyRange = keyRange;
    this.queryRange = queryRange;
  }

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);
    cache = ClientCacheFactory.getAnyInstance();
    region = cache.getRegion("region");
  }

  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    long minId = ThreadLocalRandom.current().nextLong(0, this.keyRange - queryRange);
    long maxId = minId + queryRange;

    SelectResults results = executeQuery(minId, maxId);
    verifyResults(results, minId, maxId);

    return true;
  }

  private void verifyResults(SelectResults results, long minId, long maxId) throws Exception {
    for (Object result : results) {
      long id = ((Portfolio) result).getID();
      if (id < minId || id > maxId) {
        throw new Exception("Invalid Portfolio object retrieved [min =" + minId + " max =" + maxId
            + "] Portfolio retrieved =" + ((Portfolio) result));
      }
    }
  }

  private SelectResults executeQuery(long minId, long maxId)
      throws NameResolutionException, TypeMismatchException, QueryInvocationTargetException,
      FunctionDomainException {
    QueryService queryService = cache.getQueryService();
    return (SelectResults) queryService
        .newQuery("SELECT * FROM /region r WHERE r.ID >=" + minId + " AND r.ID <=" + maxId)
        .execute();
  }
}
