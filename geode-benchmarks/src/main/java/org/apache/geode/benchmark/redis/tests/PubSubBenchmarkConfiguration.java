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


  public List<String> getChannels() {
    return IntStream.range(0, getNumChannels()).mapToObj(n -> "channel" + n)
        .collect(Collectors.toList());
  }

  public void configurePubSubTest(final RedisBenchmark benchmark,
      final TestConfig config) {
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
    after(config, new PubSubEndRedisTask(), CLIENT);
    subscriberClients.forEach(c -> after(config, new StopRedisClient(c), CLIENT));
  }
}
