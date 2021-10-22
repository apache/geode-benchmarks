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

package org.apache.geode.benchmark.redis.tasks;

import static org.apache.geode.benchmark.redis.tasks.RedisSplitKey.toKey;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.LongRange;

public class ZrangeRedisTask extends BenchmarkDriverAdapter implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(ZrangeRedisTask.class);

  private final RedisClientManager redisClientManager;
  private final LongRange keyRange;
  private final boolean validate;

  private transient LongStringCache keyCache;
  private transient RedisClient redisClient;

  public ZrangeRedisTask(final RedisClientManager redisClientManager, final LongRange keyRange,
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
    final long k = keyRange.random();

    final String key = keyCache.valueOf(toKey(k));

    final long start = ThreadLocalRandom.current()
        .nextLong(0, RedisSplitKey.NUM_PARTS_PER_KEY);
    final long len = ThreadLocalRandom.current()
        .nextLong(0, RedisSplitKey.NUM_PARTS_PER_KEY - start);
    final long stop = start + len;

    final Set<String> values = redisClient.zrange(key, start, stop);
    if (validate) {
      final LongRange range =
          new LongRange(start, stop);

      final Set<String> expectedValues =
          LongStream.range(range.getMin(), range.getMax())
              .boxed()
              .map(keyCache::valueOf)
              .collect(Collectors.toSet());

      assertThat(values).isEqualTo(expectedValues);
    }
    return true;
  }

}
