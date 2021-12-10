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

import java.util.List;
import java.util.Set;

import io.vavr.Function3;

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

  /**
   * Create a subscription listener.
   *
   * @param channelMessageConsumer a function that accepts the channel, the message, and
   *        a consumer that will unsubscribe the listener from the list of channels
   *        passed in.
   * @return the subscription listener
   */
  SubscriptionListener createSubscriptionListener(
      Function3<String, String, Unsubscriber, Void> channelMessageConsumer);

  void subscribe(SubscriptionListener control, String... channels);

  void publish(String channel, String message);

  interface SubscriptionListener {
  }

  @FunctionalInterface
  interface Unsubscriber {
    void unsubscribe(List<String> channels);
  }
}
