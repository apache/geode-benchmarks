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
import org.apache.geode.cache.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;

public class OQLQuery extends BenchmarkDriverAdapter implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(RemoteJVMFactory.class);
  private Region<Object, Object> region;
  private LongRange keyRange;
  private long queryRange;
  private ClientCache cache;
  private Query query;


  public OQLQuery(LongRange keyRange, long queryRange) {
    this.keyRange = keyRange;
    this.queryRange = queryRange;
  }

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);
    cache = ClientCacheFactory.getAnyInstance();
    region = cache.getRegion("region");
    query = cache.getQueryService().newQuery("SELECT * FROM /region r WHERE r.ID >= $1 AND r.ID < $2");
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
    for (Object result : results) {
      long id = ((Portfolio) result).getID();
      if (id < minId || id > maxId) {
        throw new Exception("Invalid Portfolio object retrieved [min =" + minId + " max =" + maxId
            + ") Portfolio retrieved =" + ((Portfolio) result));
      }
    }
  }

  private SelectResults executeQuery(long minId, long maxId) throws Exception {
    return (SelectResults) query.execute(minId, maxId);
  }
}
