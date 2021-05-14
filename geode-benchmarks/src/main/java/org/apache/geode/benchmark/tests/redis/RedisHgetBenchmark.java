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

package org.apache.geode.benchmark.tests.redis;

import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.Config.workload;
import static org.apache.geode.benchmark.tests.GeodeBenchmark.isValidationEnabled;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;

import org.apache.geode.benchmark.tasks.redis.HgetRedisTask;
import org.apache.geode.benchmark.tasks.redis.PrePopulateRedisHash;
import org.apache.geode.perftest.TestConfig;

public class RedisHgetBenchmark extends RedisBenchmark {

  @Override
  public TestConfig configure() {
    final TestConfig config = super.configure();

    before(config, new PrePopulateRedisHash(redisClientManager, keyRange), CLIENT);
    workload(config, new HgetRedisTask(redisClientManager, keyRange, isValidationEnabled()),
        CLIENT);
    return config;

  }
}
