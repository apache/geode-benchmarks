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
package benchmark.geode.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;

public class BenchmarkFunction implements Function {
  private long maxKey;
  private long minKey;

  public BenchmarkFunction(long minKey, long maxKey) {
    this.maxKey = maxKey;
    this.minKey = minKey;
  }

  @Override
  public void execute(FunctionContext context) {
    RegionFunctionContext regionFunctionContext = (RegionFunctionContext) context;
    Region region = regionFunctionContext.getDataSet();
    List<Long> results = new ArrayList<>();

    for (long i = minKey; i <= maxKey; i++) {
      Portfolio portfolio = (Portfolio) region.get(i);
      if (portfolio != null) {
        results.add(portfolio.getID());
      }
    }

    context.getResultSender().lastResult(results);
  }

  @Override
  public String getId() {
    return "BenchmarkFunction";
  }

  @Override
  public boolean isHA() {
    return false;
  }
}
