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

import static java.lang.String.valueOf;

import java.io.Serializable;
import java.util.Map;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.LongRange;

/**
 * Task workload to perform get operations on keys within 0
 * and the keyRange (exclusive)
 */
public class GetRedisTask extends BenchmarkDriverAdapter implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(GetRedisTask.class);

  private final LongRange keyRange;

  private transient long offset;
  private transient String[] keys;

  private transient ThreadLocal<StatefulRedisClusterConnection<String, String>> statefulRedisClusterConnection;

  public GetRedisTask(final LongRange keyRange) {
    this.keyRange = keyRange;
  }

  @Override
  public void setUp(final BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);

    offset = keyRange.getMin();
    keys = new String[(int) (keyRange.getMax() - offset)];
    keyRange.forEach(i -> keys[(int) i] = valueOf(i));

    statefulRedisClusterConnection = ThreadLocal.withInitial(() -> {
      logger.info("Setup for instance {} on thread {}", System.identityHashCode(this),
          Thread.currentThread().getId());

      final StatefulRedisClusterConnection<String, String> redisClusterConnection =
          RedisClusterClientSingleton.instance.connect();
      redisClusterConnection.setReadFrom(ReadFrom.ANY);
      return redisClusterConnection;
    });
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    final String key = keys[(int) (keyRange.random() - offset)];
    statefulRedisClusterConnection.get().sync().get(key);
    return true;
  }

}
