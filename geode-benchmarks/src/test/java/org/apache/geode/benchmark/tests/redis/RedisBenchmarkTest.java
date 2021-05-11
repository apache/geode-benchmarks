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

package org.apache.geode.benchmark.tests.redis;

import static java.lang.System.setProperty;
import static org.apache.geode.benchmark.tests.redis.RedisBenchmark.WITH_REDIS_CLIENT_PROPERTY;
import static org.apache.geode.benchmark.topology.Topology.WITH_SERVER_COUNT_PROPERTY;

import java.io.File;

import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.CartesianEnumSource;
import org.junitpioneer.jupiter.CartesianProductTest;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.benchmark.junit.CartesianSubclassSource;
import org.apache.geode.benchmark.junit.EnableIfClassExists;
import org.apache.geode.benchmark.tests.redis.RedisBenchmark.RedisClientImplementation;
import org.apache.geode.perftest.TestRunners;

@EnableIfClassExists("org.apache.geode.redis.internal.GeodeRedisServer")
public class RedisBenchmarkTest {

  @TempDir()
  File folder;

  @CartesianProductTest()
  @CartesianEnumSource(RedisClientImplementation.class)
  @CartesianSubclassSource(RedisBenchmark.class)
  @ClearSystemProperty(key = WITH_REDIS_CLIENT_PROPERTY)
  @SetSystemProperty(key = WITH_SERVER_COUNT_PROPERTY, value = "1")
  public void benchmarkRunsSuccessfully(final RedisClientImplementation redisClientImplementation,
      final Class<? extends RedisBenchmark> redisBenchmark)
      throws Exception {
    setProperty(WITH_REDIS_CLIENT_PROPERTY, redisClientImplementation.name());

    final RedisBenchmark test = redisBenchmark.newInstance();
    test.setKeyRange(new LongRange(0, 100));
    test.setValidationEnabled(true);

    TestRunners.minimalRunner(folder).runTest(test);
  }
}
