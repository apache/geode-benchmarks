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

import benchmark.geode.data.FunctionWithArguments;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;

public class ExecuteParameterizedFunction extends BenchmarkDriverAdapter implements Serializable {

  private final LongRange keyRange;
  private final Function function;

  private Region region;

  public ExecuteParameterizedFunction(final LongRange keyRange) {
    this.keyRange = keyRange;
    function = new FunctionWithArguments();
  }

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);
    ClientCache cache = ClientCacheFactory.getAnyInstance();
    region = cache.getRegion("region");
    FunctionService.registerFunction(function);
  }

  @Override
  public boolean test(Map<Object, Object> ctx) {
    @SuppressWarnings("unchecked")
    ResultCollector<?, ?> resultCollector = FunctionService
        .onRegion(region)
        .setArguments(keyRange.random())
        .execute(function);
    resultCollector.getResult();
    return true;
  }
}
