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

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class SubscribeTask implements Task {
  private static final Logger logger = LoggerFactory.getLogger(SubscribeTask.class);

  private final int messageLength;
  private final boolean validate;

  private transient List<Subscriber> subscribers;
  private transient ExecutorService threadPool;

  public SubscribeTask(List<RedisClientManager> subscriberClientManagers,
                       List<String> channels, int numMessagesPerChannelPerOperation,
                       int messageLength, CyclicBarrier barrier, boolean validate) {

    this.messageLength = messageLength;
    this.validate = validate;

    threadPool = Executors.newFixedThreadPool(subscriberClientManagers.size());
    int numMessagesExpected = channels.size() * numMessagesPerChannelPerOperation;
    subscribers = subscriberClientManagers.stream().map( cm ->
            new Subscriber(cm.get(), channels, numMessagesExpected, barrier))
        .collect(Collectors.toList());
  }

  @Override
  public void run(TestContext context) throws Exception {
    for (Subscriber subscriber : subscribers) {
      subscriber.subscribeAsync();
    }
  }

  public List<Subscriber> getSubscribers() {
    return subscribers;
  }

  public void shutdown() throws InterruptedException {
    for (SubscribeTask.Subscriber subscriber : subscribers) {
      subscriber.unsubscribeAllChannels();
      subscriber.waitForCompletion();
    }
    threadPool.shutdownNow();
    threadPool.awaitTermination(5, TimeUnit.MINUTES);
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
    Subscriber(RedisClient client, final List<String> channels,
        int numMessagesExpected, final CyclicBarrier barrier) {
      this.channels = channels;
      this.messagesReceived = new AtomicInteger(0);
      this.numMessagesExpected = numMessagesExpected;
      this.client = client;

      listener = client.createSubscriptionListener((channel, message) -> {
        if (receiveMessageAndIsComplete(channel, message)) {
          try {
            reset();
            barrier.await();
          }
          catch (InterruptedException | BrokenBarrierException ignored) { }
        }
      });
    }

    public void subscribeAsync() {
      future = CompletableFuture.runAsync(() ->
          client.subscribe(listener, channels.toArray(new String[] {})), threadPool);
    }

    public void unsubscribeAllChannels() {
      if (future == null) {
        return;
      }
      listener.unsubscribe(channels.toArray(new String[] {}));
    }

    public void waitForCompletion() {
      if (future == null) {
        return;
      }
      future.join();
    }

    // Receive a message and return true if all messages have been received
    private boolean receiveMessageAndIsComplete(String channel, String message) {
      if (validate) {
        logger.info(String.format("Received message %s of length %d on channel %s",
            message, message.length(), channel));
        if (message.length() != messageLength) {
          logger.error(String.format("Received message of length %d but expected length %d",
              message.length(), messageLength));
        }
      }
      return messagesReceived.incrementAndGet() >= numMessagesExpected;
    }

    private void reset() {
      messagesReceived.set(0);
    }
  }
}
