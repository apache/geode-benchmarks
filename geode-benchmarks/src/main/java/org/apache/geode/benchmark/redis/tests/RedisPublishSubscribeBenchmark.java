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
import org.apache.geode.benchmark.redis.tasks.PubSubEndRedisTask;
import org.apache.geode.benchmark.redis.tasks.PublishRedisTask;
import org.apache.geode.benchmark.redis.tasks.RedisClientManager;
import org.apache.geode.benchmark.redis.tasks.StopRedisClient;
import org.apache.geode.benchmark.redis.tasks.SubscribeRedisTask;
import org.apache.geode.perftest.TestConfig;

public class RedisPublishSubscribeBenchmark extends RedisBenchmark {

  private static final int NUM_CHANNELS = 1;
  private static final int NUM_SUBSCRIBERS = 1;
  private static final int NUM_MESSAGES_PER_CHANNEL_PER_OPERATION = 1;
  private static final int MESSAGE_LENGTH = 1;

  // This static is used for coordinating between subscribers and publisher for the
  // benchmark within a single worker JVM, and for a clean shutdown.
  // This is a transient (non-serializable) object in worker JVM.
  // All subscribers plus one publisher will wait on the barrier at completion of
  // each operation
  // WARNING: This relies on the assumption that there is only one Worker per ChildJVM
  // and also that the publisher and subscribers run in the same JVM
  // TODO: Try to find a safer way to share the barrier object between the before and workload tasks
  private static final CyclicBarrier barrier = new CyclicBarrier(NUM_SUBSCRIBERS + 1);

  // These static "getter" methods are only here for refactoring flexibility
  public static CyclicBarrier getCyclicBarrier() {
    return barrier;
  }

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

    before(config,
        new SubscribeRedisTask(subscriberClients,
            channels, NUM_MESSAGES_PER_CHANNEL_PER_OPERATION, MESSAGE_LENGTH, isValidationEnabled()),
        CLIENT);
    workload(config,
        new PublishRedisTask(redisClientManager,
            channels, NUM_MESSAGES_PER_CHANNEL_PER_OPERATION, MESSAGE_LENGTH),
        CLIENT);
    after(config, new PubSubEndRedisTask(), CLIENT);
    subscriberClients.forEach(c -> after(config, new StopRedisClient(c), CLIENT));

    return config;
  }
}
