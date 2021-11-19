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

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class SubscriptionsTask implements Task {

  private final List<RedisClientManager> subscriberClientManagers;
  private final List<String> channels;
  private final int numMessagesPerChannelPerOperation;
  private final int messageLengthInBytes;
  private final CyclicBarrier barrier;

  private transient ExecutorService threadPool;
  private transient List<Subscriber> subscribers;

  public SubscriptionsTask(List<RedisClientManager> subscriberClientManagers,
                           List<String> channels, int numMessagesPerChannelPerOperation,
                           int messageLengthInBytes, CyclicBarrier barrier) {
    this.subscriberClientManagers = subscriberClientManagers;
    this.channels = channels;
    this.numMessagesPerChannelPerOperation = numMessagesPerChannelPerOperation;
    this.messageLengthInBytes = messageLengthInBytes;
    this.barrier = barrier;
  }

  @Override
  public void run(TestContext context) throws Exception {
    int numSubscribers = subscriberClientManagers.size();

    threadPool = Executors.newFixedThreadPool(numSubscribers);
    barrier.getNumberWaiting();
    int numMessagesExpected = channels.size() * numMessagesPerChannelPerOperation;
    subscribers = subscriberClientManagers.stream().map( cm ->
      new Subscriber(cm.get(), channels, numMessagesExpected, barrier, threadPool))
      .collect(Collectors.toList());
  }

  public List<Subscriber> getSubscribers() {
    return subscribers;
  }

  public void shutdown() throws InterruptedException {
    for (SubscriptionsTask.Subscriber subscriber : subscribers) {
      subscriber.waitForCompletion();
    }
    threadPool.shutdownNow();
    threadPool.awaitTermination(5, TimeUnit.MINUTES);
  }

  public static class Subscriber {
    private final AtomicInteger messagesReceived;
    private final int numMessagesExpected;
    private final CompletableFuture<Void> future;

    /**
     *
     * @param client
     * @param channels
     * @param numMessagesExpected
     * @param barrier The semaphore-like object to wait on when received all messages
     * @param threadPool The thread pool for scheduling a subscriber thread
     */
    Subscriber(RedisClient client, final List<String> channels,
        int numMessagesExpected, final CyclicBarrier barrier, ExecutorService threadPool) {
      this.messagesReceived = new AtomicInteger(0);
      this.numMessagesExpected = numMessagesExpected;

      // TODO Refactor this so that the listener can be constructed with
      // the subscriber and barrier as parameters separately from  scheduling the thread

      future = CompletableFuture.runAsync(() -> {
        RedisClient.SubscriptionListener
            listener = client.createSubscriptionListener((channel, message) -> {
          // TODO add validation for message length
          if (receiveMessageAndIsComplete()) {
            try {
              reset();
              barrier.await();
            }
            catch (InterruptedException | BrokenBarrierException ignored) { }
          }
        });
        client.subscribe(listener, channels.toArray(new String[] {}));
      }, threadPool);
    }

    public void waitForCompletion() {
      future.join();
    }

    // Receive a message and return true if all messages have been received
    private boolean receiveMessageAndIsComplete() {
      return messagesReceived.incrementAndGet() >= numMessagesExpected;
    }

    private void reset() {
      messagesReceived.set(0);
    }
  }
}
