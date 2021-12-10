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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.redis.tests.PubSubBenchmarkConfiguration;

public class PublishRedisTask extends BenchmarkDriverAdapter implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(PublishRedisTask.class);
  private final PubSubBenchmarkConfiguration pubSubConfig;
  private final RedisClientManager publisherClientManager;

  public PublishRedisTask(final PubSubBenchmarkConfiguration pubSubConfig,
      final RedisClientManager publisherClientManager) {
    this.pubSubConfig = pubSubConfig;
    this.publisherClientManager = publisherClientManager;
    logger.info("Initialized: PublishRedisTask");
  }

  @Override
  public boolean test(final Map<Object, Object> ctx) throws Exception {
    final CyclicBarrier barrier = pubSubConfig.getCyclicBarrier();
    final RedisClient redisClient = publisherClientManager.get();

    for (final String channel : pubSubConfig.getChannels()) {
      for (int i = 0; i < pubSubConfig.getNumMessagesPerChannelOperation(); i++) {
        final String message = Strings.repeat(String.valueOf((char) ('A' + i)),
            pubSubConfig.getMessageLength());
        redisClient.publish(channel, message);
      }
    }

    // debug
    publisherClientManager.get().publish(pubSubConfig.getControlChannel(),
        pubSubConfig.getEndMessage());

    // waits for all subscribers to receive all messages, then barrier is reset automatically
    // for the next test iteration; fails test if times out
    barrier.await(10, TimeUnit.SECONDS);
    return true;
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    logger.info(String.format(
        "PublishRedisTask: Sending END message; barrier num waiting=%d;isBroken=%s",
        pubSubConfig.getCyclicBarrier().getNumberWaiting(),
        pubSubConfig.getCyclicBarrier().isBroken()));
    publisherClientManager.get().publish(pubSubConfig.getControlChannel(),
        pubSubConfig.getEndMessage());
  }
}
