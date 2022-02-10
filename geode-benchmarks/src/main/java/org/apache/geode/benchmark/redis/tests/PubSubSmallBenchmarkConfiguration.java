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

import java.util.concurrent.CyclicBarrier;

public class PubSubSmallBenchmarkConfiguration extends PubSubBenchmarkConfiguration {

  private static final int NUM_SUBSCRIBERS = 1;
  private static final int NUM_CHANNELS = 1;
  private static final int NUM_MESSAGES_PER_CHANNEL_OPERATION = 1;
  private static final int MESSAGE_LENGTH = 1;
  private static final String CONTROL_CHANNEL = "__control__";
  private static final String END_MESSAGE = "END";

  private static final CyclicBarrier BARRIER = new CyclicBarrier(NUM_SUBSCRIBERS + 1);

  private final boolean shouldUseChannelPattern;

  public PubSubSmallBenchmarkConfiguration() {
    this(false);
  }

  public PubSubSmallBenchmarkConfiguration(final boolean shouldUseChannelPattern) {
    this.shouldUseChannelPattern = shouldUseChannelPattern;
  }

  @Override
  public boolean shouldUseChannelPattern() {
    return shouldUseChannelPattern;
  }

  @Override
  public String getEndMessage() {
    return END_MESSAGE;
  }

  @Override
  public CyclicBarrier getCyclicBarrier() {
    return BARRIER;
  }

  @Override
  public int getNumSubscribers() {
    return NUM_SUBSCRIBERS;
  }

  @Override
  public int getNumChannels() {
    return NUM_CHANNELS;
  }

  @Override
  public int getNumMessagesPerChannelOperation() {
    return NUM_MESSAGES_PER_CHANNEL_OPERATION;
  }

  @Override
  public int getMessageLength() {
    return MESSAGE_LENGTH;
  }

  @Override
  public String getControlChannel() {
    return CONTROL_CHANNEL;
  }
}