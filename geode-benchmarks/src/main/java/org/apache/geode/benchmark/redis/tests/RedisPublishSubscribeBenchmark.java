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

package org.apache.geode.benchmark.redis.tests;

import static org.apache.geode.benchmark.Config.after;
import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.Config.workload;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.geode.benchmark.redis.tasks.JedisClientManager;
import org.apache.geode.benchmark.redis.tasks.LettucePubSubClientManager;
import org.apache.geode.benchmark.redis.tasks.PublishSubscribeRedisTask;
import org.apache.geode.benchmark.redis.tasks.RedisClientManager;
import org.apache.geode.benchmark.redis.tasks.StopRedisClient;
import org.apache.geode.benchmark.redis.tasks.SubscribeTask;
import org.apache.geode.perftest.TestConfig;

public class RedisPublishSubscribeBenchmark extends RedisBenchmark {

  private static final int NUM_CHANNELS = 1;
  private static final int NUM_SUBSCRIBERS = 1;
  private static final int NUM_MESSAGES_PER_CHANNEL_PER_OPERATION = 1;
  private static final int MESSAGE_LENGTH = 1;

  @Override
  public TestConfig configure() {
    final TestConfig config = super.configure();

    final List<String> channels = IntStream.range(0, NUM_CHANNELS).mapToObj(n -> "channel" + n)
            .collect(Collectors.toList());

    Supplier<RedisClientManager> clientManagerSupplier;
    switch (getRedisClientImplementation()) {
      case Jedis:
        clientManagerSupplier = JedisClientManager::new;
        break;
      case Lettuce:
        clientManagerSupplier = LettucePubSubClientManager::new;
        break;
      default:
        throw new AssertionError("unexpected RedisClientImplementation");
    }

    List<RedisClientManager> subscriberClients =
        Stream.generate(clientManagerSupplier).limit(NUM_SUBSCRIBERS)
            .collect(Collectors.toList());

    // all subscribers plus one publisher will wait on the barrier at completion of
    // each operation
    final CyclicBarrier barrier = new CyclicBarrier(NUM_SUBSCRIBERS + 1);

    SubscribeTask subscribeTask = new SubscribeTask(subscriberClients, channels,
        NUM_MESSAGES_PER_CHANNEL_PER_OPERATION, MESSAGE_LENGTH, barrier, isValidationEnabled());
    final List<SubscribeTask.Subscriber> subscribers = subscribeTask.getSubscribers();

    before(config, subscribeTask, CLIENT);
    workload(config,
        new PublishSubscribeRedisTask(redisClientManager, subscribers,
            channels, NUM_MESSAGES_PER_CHANNEL_PER_OPERATION, MESSAGE_LENGTH, barrier),
        CLIENT);
    after(config, new PubSubEndTask("control", redisClientManager, subscribeTask), CLIENT);
    subscriberClients.forEach(c -> after(config, new StopRedisClient(c), CLIENT));

    return config;
  }
}
