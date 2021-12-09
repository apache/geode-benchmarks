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

package org.apache.geode.benchmark.redis.tests;

import static org.apache.geode.benchmark.Config.after;
import static org.apache.geode.benchmark.Config.before;
import static org.apache.geode.benchmark.Config.workload;
import static org.apache.geode.benchmark.topology.Roles.CLIENT;

import java.io.Serializable;
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
import org.apache.geode.benchmark.redis.utils.Lazy;
import org.apache.geode.perftest.TestConfig;

public class PubSubBenchmarkHelper implements Serializable {

  private Lazy<CyclicBarrier> lazyCyclicBarrier = new Lazy<>();

  private final int numSubscribers;

  public CyclicBarrier getCyclicBarrier() {
    // only create one CyclicBarrier in each JVM per benchmark test
    // for synchronization between the publisher and subscribers.
    // Will create a new CyclicBarrier once after being serialized, using
    // the numSubscribers parameter.
    return lazyCyclicBarrier.get(() -> new CyclicBarrier(numSubscribers + 1));
  }

  private static final int NUM_SUBSCRIBERS = 1;
  public static final CyclicBarrier BARRIER = new CyclicBarrier(NUM_SUBSCRIBERS + 1);

  public PubSubBenchmarkHelper(int numSubscribers) {
    this.numSubscribers = numSubscribers;
  }

  public void configurePubSubTest(RedisBenchmark benchmark,
      TestConfig config,
      int numChannels,
      int numMessagesPerChannelPerOperation,
      int messageLength) {
    final List<String> channels = IntStream.range(0, numChannels).mapToObj(n -> "channel" + n)
        .collect(Collectors.toList());

    Supplier<RedisClientManager> clientManagerSupplier;
    switch (benchmark.getRedisClientImplementation()) {
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
        Stream.generate(clientManagerSupplier).limit(numSubscribers)
            .collect(Collectors.toList());

    before(config,
        new SubscribeRedisTask(this, subscriberClients,
            channels, numMessagesPerChannelPerOperation, messageLength,
            benchmark.isValidationEnabled()),
        CLIENT);
    workload(config,
        new PublishRedisTask(this, benchmark.redisClientManager,
            channels, numMessagesPerChannelPerOperation, messageLength),
        CLIENT);
    after(config, new PubSubEndRedisTask(), CLIENT);
    subscriberClients.forEach(c -> after(config, new StopRedisClient(c), CLIENT));
  }
}
