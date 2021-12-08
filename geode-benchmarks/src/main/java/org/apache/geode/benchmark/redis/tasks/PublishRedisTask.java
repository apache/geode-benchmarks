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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.redis.tests.RedisPublishSubscribeBenchmark;

public class PublishRedisTask extends BenchmarkDriverAdapter implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(PublishRedisTask.class);
  private final int numMessages;
  private final int messageLength;
  private final RedisClientManager publisherClientManager;
  private final List<String> channels;

  public PublishRedisTask(final RedisClientManager publisherClientManager,
                          List<String> channels, int numMessages, int messageLength) {
    this.publisherClientManager = publisherClientManager;
    logger.info("Initialized: PublishSubscribeRedisTask");
    this.messageLength = messageLength;
    this.numMessages = numMessages;
    this.channels = channels;
  }

  @Override
  public boolean test(final Map<Object, Object> ctx) throws Exception {
    CyclicBarrier barrier = RedisPublishSubscribeBenchmark.getCyclicBarrier();
    RedisClient redisClient = publisherClientManager.get();

    for (String channel : channels) {
      for (int i = 0; i < numMessages; i++) {
        String message = Strings.repeat(String.valueOf((char)('A' + i)), messageLength);
        redisClient.publish(channel, message);
      }
    }

    // waits for all subscribers to receive all messages, then barrier is reset automatically
    // for the next test iteration
    barrier.await();
    return true;
  }
}
