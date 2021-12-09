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

import java.util.concurrent.CyclicBarrier;

import org.apache.geode.perftest.TestConfig;

public class RedisPubSubLargeBenchmark extends RedisBenchmark {

  private static final int NUM_SUBSCRIBERS = 10;
  private static final int NUM_CHANNELS = 5;
  private static final int NUM_MESSAGES_PER_CHANNEL_OPERATION = 100;
  private static final int MESSAGE_LENGTH = 1000;

  // barrier must be a singleton in each JVM for each test
  // add one party for the publisher
  private static final CyclicBarrier BARRIER = new CyclicBarrier(NUM_SUBSCRIBERS + 1);

  @Override
  public TestConfig configure() {
    final TestConfig config = super.configure();
    PubSubBenchmarkHelper helper = new PubSubBenchmarkHelper(BARRIER);
    helper.configurePubSubTest(this, config, NUM_SUBSCRIBERS, NUM_CHANNELS,
        NUM_MESSAGES_PER_CHANNEL_OPERATION, MESSAGE_LENGTH);
    return config;
  }
}
