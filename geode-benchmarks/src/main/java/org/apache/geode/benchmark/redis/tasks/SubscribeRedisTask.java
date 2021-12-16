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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.benchmark.redis.tests.PubSubBenchmarkConfiguration;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class SubscribeRedisTask implements Task {
  private static final Logger logger = LoggerFactory.getLogger(SubscribeRedisTask.class);

  // TextContext keys for shared objects between the SubscribeTask (before) and
  // the PubSubEndTask (after)
  private static final String SUBSCRIBERS_CONTEXT_KEY = "subscribers";
  private static final String SUBSCRIBERS_THREAD_POOL = "threadPool";

  private final List<RedisClientManager> subscriberClientManagers;
  private final boolean validate;
  private final PubSubBenchmarkConfiguration pubSubConfig;

  public SubscribeRedisTask(final PubSubBenchmarkConfiguration pubSubConfig,
      final List<RedisClientManager> subscriberClientManagers,
      final boolean validate) {
    this.pubSubConfig = pubSubConfig;
    logger.info(
        "Initialized: SubscribeRedisTask numChannels={}, numMessagesPerChannel={}, messageLength={}, validate={}",
        pubSubConfig.getNumChannels(), pubSubConfig.getNumMessagesPerChannelOperation(),
        pubSubConfig.getMessageLength(), validate);
    this.subscriberClientManagers = subscriberClientManagers;
    this.validate = validate;
  }

  @Override
  public void run(final TestContext context) throws Exception {
    final CyclicBarrier barrier = pubSubConfig.getCyclicBarrier();

    // save subscribers in the TestContext, as this will be shared with
    // the after tasks which will call shutdown()
    final List<Subscriber> subscribers = subscriberClientManagers.stream()
        .map(cm -> new Subscriber(cm.get(), barrier, context))
        .collect(Collectors.toList());
    context.setAttribute(SUBSCRIBERS_CONTEXT_KEY, subscribers);

    // save thread pool in TestContext, so it can be shutdown cleanly after
    final ExecutorService subscriberThreadPool =
        Executors.newFixedThreadPool(subscriberClientManagers.size());
    context.setAttribute(SUBSCRIBERS_THREAD_POOL, subscriberThreadPool);

    for (final Subscriber subscriber : subscribers) {
      subscriber.subscribeAsync(subscriberThreadPool, context);
    }
  }

  public static void shutdown(final TestContext cxt) throws Exception {
    // precondition: method run has been previously executed in this Worker
    // and therefore subscribers and threadPool are available
    @SuppressWarnings("unchecked")
    final List<Subscriber> subscribers =
        (List<Subscriber>) cxt.getAttribute(SUBSCRIBERS_CONTEXT_KEY);

    for (final SubscribeRedisTask.Subscriber subscriber : subscribers) {
      subscriber.waitForCompletion(cxt);
    }

    logger.info("Shutting down thread pool…");

    final ExecutorService threadPool = (ExecutorService) cxt.getAttribute(SUBSCRIBERS_THREAD_POOL);
    threadPool.shutdownNow();
    // noinspection ResultOfMethodCallIgnored
    threadPool.awaitTermination(5, TimeUnit.MINUTES);

    logger.info("Thread pool terminated");
  }

  public class Subscriber {
    private final AtomicInteger messagesReceived;
    private final int numMessagesExpected;
    private final RedisClient client;
    private final RedisClient.SubscriptionListener listener;
    private CompletableFuture<Void> future;

    Subscriber(final RedisClient client, final CyclicBarrier barrier, final TestContext context) {
      this.messagesReceived = new AtomicInteger(0);
      this.client = client;

      numMessagesExpected =
          pubSubConfig.getNumChannels() * pubSubConfig.getNumMessagesPerChannelOperation();

      listener = client.createSubscriptionListener(pubSubConfig,
          (String channel, String message, RedisClient.Unsubscriber unsubscriber) -> {
            if (channel.equals(pubSubConfig.getControlChannel())) {
              if (message.equals(pubSubConfig.getEndMessage())) {
                unsubscriber.unsubscribe(pubSubConfig.getAllSubscribeChannels());
                logger.info("Subscriber thread unsubscribed.");
              } else {
                throw new AssertionError("Unrecognized control message: " + message);
              }
            } else if (receiveMessageAndIsComplete(channel, message, context)) {
              try {
                reset();
                barrier.await(10, TimeUnit.SECONDS);
              } catch (final TimeoutException e) {
                throw new RuntimeException("Subscriber timed out while waiting on barrier");
              } catch (final InterruptedException | BrokenBarrierException ignored) {
              }
            }
            return null;
          });
    }

    public void subscribeAsync(final ExecutorService threadPool, final TestContext context) {
      future = CompletableFuture.runAsync(
          () -> {
            final List<String> channels = pubSubConfig.getAllSubscribeChannels();
            if (pubSubConfig.useChannelPattern()) {
              context.logProgress("Subscribing to channel patterns " + channels);
              client.psubscribe(listener, channels.toArray(new String[]{}));
            } else {
              context.logProgress("Subscribing to channels " + channels);
              client.subscribe(listener, channels.toArray(new String[]{}));
            }
          }, threadPool);
      future.whenComplete((result, ex) -> {
        logger.info("Subscriber thread completed");
        if (ex != null) {
          ex.printStackTrace();
          context.logProgress(String.format("Subscriber completed with exception '%s')", ex));
        }
      });
    }

    public void waitForCompletion(final TestContext ctx) throws Exception {
      if (future == null) {
        return;
      }
      assertThat(future.get(10, TimeUnit.SECONDS)).isNull();
    }

    // Receive a message and return true if all messages have been received
    private boolean receiveMessageAndIsComplete(final String channel, final String message,
        final TestContext context) {
      if (validate) {
        context.logProgress(String.format(
            "Received message %s of length %d on channel %s; messagesReceived=%d; messagesExpected=%d",
            message, message.length(), channel, messagesReceived.get() + 1, numMessagesExpected));
        assertThat(message.length()).isEqualTo(pubSubConfig.getMessageLength());
      }
      return messagesReceived.incrementAndGet() >= numMessagesExpected;
    }

    private void reset() {
      messagesReceived.set(0);
    }
  }
}
