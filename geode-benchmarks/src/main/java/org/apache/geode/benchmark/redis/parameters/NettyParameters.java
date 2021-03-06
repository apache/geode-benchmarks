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

package org.apache.geode.benchmark.redis.parameters;

import static java.lang.Integer.getInteger;
import static java.lang.String.format;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.TestConfig;

public class NettyParameters {
  private static final Logger logger = LoggerFactory.getLogger(NettyParameters.class);
  public static final String WITH_NETTY_THREADS = "benchmark.withNettyThreads";

  public static void configure(final TestConfig testConfig) {
    logger.info("Configuring Netty parameters.");

    final Integer withNettyThreads = getInteger(WITH_NETTY_THREADS, null);
    if (null != withNettyThreads) {
      testConfig.jvmArgs(SERVER.name(), format("-Dio.netty.eventLoopThreads=%d", withNettyThreads));
    }
  }

}
