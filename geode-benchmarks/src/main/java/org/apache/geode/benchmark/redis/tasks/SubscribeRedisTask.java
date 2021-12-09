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
import java.util.function.Consumer;
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
    final int numMessagesExpected =
        pubSubConfig.getNumChannels() * pubSubConfig.getNumMessagesPerChannelOperation();

    final CyclicBarrier barrier = pubSubConfig.getCyclicBarrier();

    // save subscribers in the TestContext, as this will be shared with
    // the after tasks which will call shutdown()
    final List<Subscriber> subscribers = subscriberClientManagers.stream()
        .map(cm -> new Subscriber(cm.get(), pubSubConfig.getAllChannels(),
            numMessagesExpected, barrier, context))
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

    cxt.logProgress("Shutting down subscribers");

    for (final SubscribeRedisTask.Subscriber subscriber : subscribers) {
      subscriber.unsubscribeAllChannels(cxt);
      subscriber.waitForCompletion(cxt);
    }

    cxt.logProgress("Shutting down thread pool…");

    final ExecutorService threadPool = (ExecutorService) cxt.getAttribute(SUBSCRIBERS_THREAD_POOL);
    threadPool.shutdownNow();
    // noinspection ResultOfMethodCallIgnored
    threadPool.awaitTermination(5, TimeUnit.MINUTES);

    cxt.logProgress("Thread pool terminated");
  }

  public class Subscriber {
    private final AtomicInteger messagesReceived;
    private final int numMessagesExpected;
    private final RedisClient client;
    private final RedisClient.SubscriptionListener listener;
    private final List<String> channels;
    private CompletableFuture<Void> future;

    /**
     *
     * @param client the RedisClient
     * @param channels list of channels to subscribe to
     * @param numMessagesExpected total number of messages expected
     * @param barrier Wait on this when received all messages
     */
    Subscriber(final RedisClient client, final List<String> channels,
        final int numMessagesExpected, final CyclicBarrier barrier, final TestContext context) {
      this.channels = channels;
      this.messagesReceived = new AtomicInteger(0);
      this.numMessagesExpected = numMessagesExpected;
      this.client = client;

      listener = client.createSubscriptionListener(
          (String channel, String message, Consumer<List<String>> unsubscriber) -> {
            if (channel.equals(pubSubConfig.getControlChannel())) {
              if (message.equals("END")) {
                unsubscriber.accept(pubSubConfig.getAllChannels());
              } else {
                throw new AssertionError("Unrecognized control message: " + message);
              }
            } else if (receiveMessageAndIsComplete(channel, message, context)) {
              try {
                reset();
                barrier.await(2, TimeUnit.SECONDS);
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
          () -> client.subscribe(listener, channels.toArray(new String[] {})), threadPool);
    }

    public void unsubscribeAllChannels(final TestContext ctx) {
      if (future == null) {
        return;
      }
      ctx.logProgress("Unsubscribing to channels " + channels);

      // TODO unsubscribe is not working, getting connection exception
      // listener.unsubscribe(channels.toArray(new String[] {}));
      ctx.logProgress("(Unsubscribe was no-opped out)");
    }

    public void waitForCompletion(final TestContext ctx) throws Exception {
      // TODO Getting an unexpected end of stream error from the subscriber thread
      // I believe the problem is that pubsub is not thread-safe, so requires
      // the subscriber thread to (somehow) unsubscribe itself.
      /*
       * if (future == null) {
       * return;
       * }
       * ctx.logProgress("Waiting for completion");
       * assertThat(future.get(2, TimeUnit.SECONDS)).isNull();
       * ctx.logProgress("Joined with subscriber thread");
       */
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
