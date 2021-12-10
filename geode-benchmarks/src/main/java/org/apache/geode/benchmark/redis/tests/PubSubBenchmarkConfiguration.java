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
import org.apache.geode.perftest.TestConfig;

public abstract class PubSubBenchmarkConfiguration implements Serializable {

  public abstract CyclicBarrier getCyclicBarrier();

  public abstract int getNumSubscribers();

  public abstract int getNumChannels();

  public abstract int getNumMessagesPerChannelOperation();

  public abstract int getMessageLength();

  public abstract String getControlChannel();

  public abstract String getEndMessage();

  public List<String> getChannels() {
    return IntStream.range(0, getNumChannels()).mapToObj(n -> "channel" + n)
        .collect(Collectors.toList());
  }

  /** Return list of all channels including the control channel. */
  public List<String> getAllChannels() {
    return Stream.concat(getChannels().stream(),
        Stream.of(getControlChannel())).collect(Collectors.toList());
  }

  public void configurePubSubTest(final RedisBenchmark benchmark,
      final TestConfig config) {

    // By design this benchmark must be run single-threaded
    config.threads(1);

    final Supplier<RedisClientManager> clientManagerSupplier;
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

    final List<RedisClientManager> subscriberClients =
        Stream.generate(clientManagerSupplier).limit(getNumSubscribers())
            .collect(Collectors.toList());

    before(config,
        new SubscribeRedisTask(this, subscriberClients,
            benchmark.isValidationEnabled()),
        CLIENT);
    workload(config,
        new PublishRedisTask(this, benchmark.redisClientManager),
        CLIENT);

    // it's no use trying to shutdown the thread pool, the StopRedisClient after task
    // runs in parallel with other after tasks introducing a race condition
    after(config, new PubSubEndRedisTask(this, benchmark.redisClientManager), CLIENT);
    subscriberClients.forEach(c -> after(config, new StopRedisClient(c), CLIENT));
  }

}
