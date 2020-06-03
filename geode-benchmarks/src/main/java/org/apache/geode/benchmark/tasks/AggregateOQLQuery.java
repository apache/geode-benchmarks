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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;

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

  private static final Logger logger = LoggerFactory.getLogger(RemoteJVMFactory.class);
  private Region<Object, Object> region;
  private LongRange keyRange;
  private long queryRange;
  private String type;
  ClientCache cache;
  private Query query;

  public AggregateOQLQuery(LongRange keyRange, long queryRange, String type) {
    this.keyRange = keyRange;
    this.queryRange = queryRange;
    this.type = type;
  }

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);
    cache = ClientCacheFactory.getAnyInstance();
    region = cache.getRegion("region");
    query = cache.getQueryService()
        .newQuery("SELECT " + type + "r.ID) FROM /region r WHERE r.ID >= $1 AND r.ID <= $2");
  }

  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    long minId =
        ThreadLocalRandom.current().nextLong(keyRange.getMin(), keyRange.getMax() - queryRange);
    long maxId = minId + queryRange;

    SelectResults results = executeQuery(minId, maxId);
    verifyResults(results, minId, maxId);

    return true;
  }

  private void verifyResults(SelectResults results, long minId, long maxId) throws Exception {
    switch (type) {
      case QueryTypes.SUM:
      case QueryTypes.SUM_DISTINCT:
        long sum = (int) results.asList().get(0);
        long expectedSum = (maxId + minId) * (maxId - minId + 1) / 2;
        if (sum != expectedSum) {
          throw new Exception(
              "Incorrect query result. Expected sum was " + expectedSum + ", actual sum was " + sum
                  + ". min =" + minId + " max =" + maxId + " range =" + queryRange);
        }
        break;
      case QueryTypes.AVG:
      case QueryTypes.AVG_DISTINCT:
        long avg = (int) results.asList().get(0);
        long expectedAvg = (maxId + minId) * (maxId - minId + 1) / ((queryRange + 1) * 2);
        if (avg != expectedAvg) {
          throw new Exception(
              "Incorrect query result. Expected average was " + expectedAvg
                  + ", actual average was "
                  + avg + ". min =" + minId + " max =" + maxId + " range =" + queryRange);
        }
        break;
      case QueryTypes.COUNT:
      case QueryTypes.COUNT_DISTINCT:
        long count = (int) results.asList().get(0);
        if (count != queryRange + 1) {
          throw new Exception(
              "Incorrect query result. Expected count was " + queryRange + ", actual count was "
                  + count + ". min =" + minId + " max =" + maxId + " range =" + queryRange);
        }
        break;
      case QueryTypes.MAX:
        long max = (long) results.asList().get(0);
        if (max != maxId) {
          throw new Exception(
              "Incorrect query result. Expected max was " + maxId + ", actual max was " + max
                  + ". min =" + minId + " max =" + maxId + " range =" + queryRange);
        }
        break;
      case QueryTypes.MIN:
        long min = (long) results.asList().get(0);
        if (min != minId) {
          throw new Exception(
              "Incorrect query result. Expected min was " + minId + ", actual min was " + min
                  + ". min =" + minId + " max =" + maxId + " range =" + queryRange);
        }
        break;
      default:
        throw new Exception("Unsupported aggregate query type: " + type);
    }
  }

  private SelectResults executeQuery(long minId, long maxId) throws Exception {
    return (SelectResults) query.execute(minId, maxId);
  }
}
