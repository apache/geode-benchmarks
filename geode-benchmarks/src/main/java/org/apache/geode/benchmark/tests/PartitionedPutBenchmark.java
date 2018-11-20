/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.benchmark.tests;

import org.junit.Test;

import org.apache.geode.benchmark.tasks.PutTask;
import org.apache.geode.benchmark.tasks.StartClient;
import org.apache.geode.benchmark.tasks.StartLocator;
import org.apache.geode.benchmark.tasks.StartServer;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestRunners;

public class PartitionedPutBenchmark {

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this::configure);
  }

  public void configure(TestConfig config) {

    int locatorPort = 10334;

    config.name(PartitionedPutBenchmark.class.getCanonicalName());
    config.warmupSeconds(5);
    config.durationSeconds(30);
    config.role("locator", 1);
    config.role("server", 1);
    config.role("client", 1);
    config.before(new StartLocator(locatorPort), "locator");
    config.before(new StartServer(locatorPort), "server");
    config.before(new StartClient(locatorPort), "client");
    config.workload(new PutTask(),"client");
  }
}
