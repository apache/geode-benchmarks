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

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.query.FunctionDomainException;
import org.apache.geode.cache.query.NameResolutionException;
import org.apache.geode.cache.query.QueryInvocationTargetException;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.TypeMismatchException;

public class AggregateOQLQuery extends BenchmarkDriverAdapter implements Serializable {

  public static class QueryTypes {
    public static final String SUM = "SUM(";
    public static final String SUM_DISTINCT = "SUM(DISTINCT ";
    public static final String AVG = "AVG(";
    public static final String AVG_DISTINCT = "AVG(DISTINCT ";
    public static final String COUNT = "COUNT(";
    public static final String COUNT_DISTINCT = "COUNT(DISTINCT ";
    public static final String MAX = "MAX(";
    public static final String MIN = "MIN(";
  }

  private long keyRange;
  private long queryRange;
  private String type;
  ClientCache cache;

  public AggregateOQLQuery(long keyRange, long queryRange, String type) {
    this.keyRange = keyRange;
    this.queryRange = queryRange;
    this.type = type;
  }

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);
    cache = ClientCacheFactory.getAnyInstance();
  }

  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    long minId = ThreadLocalRandom.current().nextLong(0, this.keyRange - queryRange);
    long maxId = minId + queryRange;

    executeQuery(minId, maxId, type);

    return true;
  }

  private SelectResults executeQuery(long minId, long maxId, String type)
      throws NameResolutionException, TypeMismatchException, QueryInvocationTargetException,
      FunctionDomainException {
    QueryService queryService = cache.getQueryService();
    return (SelectResults) queryService
        .newQuery("SELECT " + type + "r.ID) FROM /region r WHERE r.ID >=" + minId + " AND r.ID <="
            + maxId)
        .execute();
  }
}
