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

package org.apache.geode.benchmark.redis.tasks;

import java.util.Set;

public interface RedisClient {
  String get(String key);

  String set(String key, String value);

  String hget(String key, String field);

  boolean hset(String key, String field, String value);

  void flushdb();

  long zadd(String key, double score, String value);

  long zrem(String key, String value);

  Set<String> zrange(String key, long start, long stop);

  Set<String> zrangeByScore(String key, long start, long stop);
}
