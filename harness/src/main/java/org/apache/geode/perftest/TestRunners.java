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

package org.apache.geode.perftest;

import java.io.File;

import org.apache.geode.perftest.infrastructure.local.LocalInfrastructureFactory;
import org.apache.geode.perftest.infrastructure.ssh.SshInfrastructureFactory;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;
import org.apache.geode.perftest.runner.DefaultTestRunner;

/**
 * Static factory methods for implementations of {@link TestRunner}
 *
 * This the main entry point for running performance tests. Users of this
 * class should create a {@link PerformanceTest} and pass it to the
 * {@link TestRunner#runTest(PerformanceTest)}
 * method of the test runner. For example
 * <code>
 *   TestRunners.default().runTest(new YourPerformanceTest());
 * </code>
 *
 */
public class TestRunners {

  public static final String TEST_HOSTS = "TEST_HOSTS";
  public static final String OUTPUT_DIR = "OUTPUT_DIR";


  public static TestRunner defaultRunner(String username, File outputDir, String... hosts) {
    return new DefaultTestRunner(
        new RemoteJVMFactory(new SshInfrastructureFactory(username, hosts)),
        outputDir);
  }

  /**
   * The default runner, which gets a list of hosts to run on from the
   * TEST_HOSTS system property.
   *
   */
  public static TestRunner defaultRunner() {
    String testHosts = System.getProperty(TEST_HOSTS);
    String outputDir = System.getProperty(OUTPUT_DIR, "output");

    return defaultRunner(testHosts, new File(outputDir));
  }

  static TestRunner defaultRunner(String testHosts, File outputDir) {
    if (testHosts == null) {
      throw new IllegalStateException(
          "You must set the TEST_HOSTS system property to a comma separated list of hosts to run the benchmarks on.");
    }

    String userName = System.getProperty("user.name");
    return defaultRunner(userName, outputDir, testHosts.split(",\\s*"));
  }

  /**
   * A test runner that runs the test with the minimal tuning - only
   * 1 second duration on local infrastructure.
   */
  public static TestRunner minimalRunner(final File outputDir) {
    return new DefaultTestRunner(new RemoteJVMFactory(new LocalInfrastructureFactory()),
        outputDir) {
      @Override
      public void runTest(TestConfig config) throws Exception {
        config.warmupSeconds(0);
        config.durationSeconds(1);
        config.threads(1);
        super.runTest(config);
      }
    };
  }

}
