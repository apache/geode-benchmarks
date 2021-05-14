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
import static org.apache.geode.benchmark.tasks.redis.RedisHash.toField;
import static org.apache.geode.benchmark.tasks.redis.RedisHash.toKey;

import org.apache.geode.benchmark.LongRange;

public class PrePopulateRedisHash extends AbstractPrePopulate {

  public PrePopulateRedisHash(
      final RedisClientManager redisClientManager,
      final LongRange keyRangeToPrepopulate) {
    super(redisClientManager, keyRangeToPrepopulate);
  }

  @Override
  protected void prepopulate(final RedisClient redisClient, final long key) {
    final String value = valueOf(toField(key));
    redisClient.hset(valueOf(toKey(key)), value, value);
  }
}
