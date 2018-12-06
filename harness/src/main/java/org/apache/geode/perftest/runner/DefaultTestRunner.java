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

package org.apache.geode.perftest.runner;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestRunner;
import org.apache.geode.perftest.infrastructure.InfrastructureFactory;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;
import org.apache.geode.perftest.jvms.RemoteJVMs;

/**
 * Runner that executes a {@link PerformanceTest}, using
 * a provided {@link InfrastructureFactory}.
 *
 * This is the main entry point for running tests. Users should
 * implement {@link PerformanceTest} to define there tests in
 * a declarative fashion and then execute them this runner.
 */
public class DefaultTestRunner implements TestRunner {
  private static final Logger logger = LoggerFactory.getLogger(DefaultTestRunner.class);


  private final RemoteJVMFactory remoteJvmFactory;
  private File outputDir;

  public DefaultTestRunner(RemoteJVMFactory remoteJvmFactory, File outputDir) {
    this.remoteJvmFactory = remoteJvmFactory;
    this.outputDir = outputDir;
  }

  @Override
  public void runTest(PerformanceTest test) throws Exception {
    TestConfig config = test.configure();
    runTest(config);
  }

  protected void runTest(TestConfig config)
      throws Exception {
    int nodes = config.getTotalJVMs();

    if (config.getName() == null) {
      throw new IllegalStateException("Benchmark must have a name.");
    }
    File benchmarkOutput = new File(outputDir, config.getName());
    if (benchmarkOutput.exists()) {
      throw new IllegalStateException(
          "Benchmark output directory already exists: " + benchmarkOutput.getPath());
    }


    Map<String, Integer> roles = config.getRoles();
    Map<String, List<String>> jvmArgs = config.getJvmArgs();

    logger.info("Lauching JVMs...");
    // launch JVMs in parallel, hook them up
    try (RemoteJVMs remoteJVMs = remoteJvmFactory.launch(roles, jvmArgs)) {

      logger.info("Starting before tasks...");
      runTasks(config.getBefore(), remoteJVMs);

      logger.info("Starting workload tasks...");
      runTasks(config.getWorkload(), remoteJVMs);

      logger.info("Starting after tasks...");
      runTasks(config.getAfter(), remoteJVMs);

      logger.info("Copying results...");
      remoteJVMs.copyResults(benchmarkOutput);

    }
  }

  private void runTasks(List<TestConfig.TestStep> steps,
      RemoteJVMs remoteJVMs) {
    steps.forEach(testStep -> {
      remoteJVMs.execute(testStep.getTask(), testStep.getRoles());
    });
  }

  public RemoteJVMFactory getRemoteJvmFactory() {
    return remoteJvmFactory;
  }
}
