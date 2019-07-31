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

import java.io.Serializable;
import java.util.Map;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.pdx.JSONFormatter;

public class CreatePdxFromJSONTask extends BenchmarkDriverAdapter
    implements Serializable {

  // The number of PdxTypes to create per test execution.
  private int batchSize;

  private int count = 0;

  private String member = null;

  public CreatePdxFromJSONTask(int batchSize) {
    this.batchSize = batchSize;
  }

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    // The test is by operationCounts, not by duration
    // We are creating new pdxTypes, no need to warm up
    // The performance is impacted on how many total pdxType when creating a
    // new one. Each test starts from creating 10K new pdxType.
    // Creating an existing pdxType will not impact the performance.
    cfg.operationsCount(batchSize);
    cfg.warmup(0);
    cfg.duration(0);
    super.setUp(cfg);
    if (member == null) {
      member = CacheFactory.getAnyInstance().getDistributedSystem()
          .getDistributedMember().getName();
    }
  }

  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    String field =
        "\"" + member + "-" + Thread.currentThread().getName() + "-" + count +
            "\": " + count;
    String jsonString = "{" + field + "}";
    JSONFormatter.fromJSON(jsonString);
    count++;
    return true;
  }
}
