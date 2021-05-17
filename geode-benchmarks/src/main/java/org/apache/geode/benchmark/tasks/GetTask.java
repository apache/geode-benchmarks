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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Map;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;

/**
 * Task workload to perform get operations on keys within 0
 * and the keyRange (exclusive)
 */
public class GetTask extends BenchmarkDriverAdapter implements Serializable {

  private final LongRange keyRange;
  private final boolean isValidationEnabled;

  private Region<Long, Object> region;

  public GetTask(LongRange keyRange, final boolean isValidationEnabled) {
    this.keyRange = keyRange;
    this.isValidationEnabled = isValidationEnabled;
  }

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);

    final Cache cache = CacheFactory.getAnyInstance();
    region = cache.getRegion("region");
  }

  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    final long key = keyRange.random();
    final Object result = region.get(key);

    if (isValidationEnabled) {
      assertThat(result).isNotNull();
    }

    return true;
  }
}
