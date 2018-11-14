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

package org.apache.geode.perftest;

import org.apache.geode.perftest.infrastructure.InfrastructureFactory;
import org.apache.geode.perftest.infrastructure.local.LocalInfrastructureFactory;
import org.apache.geode.perftest.infrastructure.ssh.SshInfrastructureFactory;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;
import org.apache.geode.perftest.runner.DefaultTestRunner;

/**
 * Static factory methods for implementations of {@link TestRunner}
 *
 * This the main entry point for running performance tests. Users of this
 * class should create a {@link PerformanceTest} and pass it to the {@link TestRunner#runTest(PerformanceTest)}
 * method of the test runner. For example
 * <code>
 *   TestRunners.default().runTest(new YourPerformanceTest());
 * </code>
 *
 */
public class TestRunners {

  /**
   * The default runner, which runs on localhost, unless the environment variable
   * TEST_HOSTS is set to a comma separated list of hosts.
   *
   * If TEST_HOSTS is set, the test will run on those hosts.
   */
  public static TestRunner defaultRunner() {
    String testHostsString = System.getenv("TEST_HOSTS");
    InfrastructureFactory infrastructureFactory;
    if(testHostsString == null) {
      infrastructureFactory = new LocalInfrastructureFactory();
    } else {
      String[] hosts = testHostsString.split(",\\s*");
      infrastructureFactory = new SshInfrastructureFactory(System.getProperty("user.name"), hosts);
    }

    return new DefaultTestRunner(infrastructureFactory, new RemoteJVMFactory());
  }
}
