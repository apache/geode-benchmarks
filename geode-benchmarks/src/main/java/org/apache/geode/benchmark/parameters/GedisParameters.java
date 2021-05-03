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

package org.apache.geode.benchmark.parameters;

import static java.lang.Integer.getInteger;
import static java.lang.String.format;
import static org.apache.geode.benchmark.tests.GeodeBenchmark.WITH_BUCKETS;
import static org.apache.geode.benchmark.tests.GeodeBenchmark.WITH_REPLICAS;
import static org.apache.geode.benchmark.topology.Roles.SERVER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.TestConfig;

public class GedisParameters {
  private static final Logger logger = LoggerFactory.getLogger(GedisParameters.class);

  public static void configure(final TestConfig testConfig) {
    logger.info("Configuring Gedis parameters.");

    testConfig.jvmArgs(SERVER.name(), "-Denable-unsupported-commands=true",
        format("-Dredis.replicas=%d", getInteger(WITH_REPLICAS, 1)),
        format("-Dredis.region.buckets=%d", getInteger(WITH_BUCKETS, 128)),
        format("-Djava.lang.Integer.IntegerCache.high=%d", getInteger(WITH_BUCKETS, 128)));
  }

}
