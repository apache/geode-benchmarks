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

package org.apache.geode.benchmark.tasks.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.LongRange;


public class GetRedisTask extends BenchmarkDriverAdapter implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(GetRedisTask.class);

  private final RedisClientManager redisClientManager;
  private final LongRange keyRange;
  private final boolean validate;

  private LongStringCache keyCache;
  private transient RedisClient redisClient;

  public GetRedisTask(final RedisClientManager redisClientManager, final LongRange keyRange,
      final boolean validate) {
    logger.info("Initialized: keyRange={}, validate={}", keyRange, validate);
    this.redisClientManager = redisClientManager;
    this.keyRange = keyRange;
    this.validate = validate;
  }

  @Override
  public void setUp(final BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);

    keyCache = new LongStringCache(keyRange);
    redisClient = redisClientManager.get();
  }

  @Override
  public boolean test(final Map<Object, Object> ctx) throws Exception {
    final String key = keyCache.valueOf(keyRange.random());
    final String value = redisClient.get(key);
    if (validate) {
      assertThat(value).isEqualTo(key);
    }
    return true;
  }

}
