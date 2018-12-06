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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.perftest.benchmarks.EmptyBenchmark;
import org.apache.geode.perftest.infrastructure.local.LocalInfrastructureFactory;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;
import org.apache.geode.perftest.runner.DefaultTestRunner;
import org.apache.geode.perftest.yardstick.analysis.YardstickThroughputSensorParser;

public class TestRunnerIntegrationTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private TestRunner runner;
  private File outputDir;
  public static final String SAMPLE_BENCHMARK = "SampleBenchmark";

  @Before
  public void setup() throws IOException {
    outputDir = temporaryFolder.newFolder();
    runner = new DefaultTestRunner(new RemoteJVMFactory(new LocalInfrastructureFactory()),
        outputDir);
  }

  @Test
  public void runsBeforeWorkload() throws Exception {
    runner.runTest(() -> {
      TestConfig testConfig = new TestConfig();
      testConfig.name(SAMPLE_BENCHMARK);
      testConfig.role("all", 1);
      testConfig.before(context -> System.out.println("hello"), "all");
      return testConfig;
    });
  }

  @Test
  public void generatesOutputDirectoryPerBenchmark() throws Exception {

    runner.runTest(() -> {
      TestConfig testConfig = new TestConfig();
      testConfig.name(SAMPLE_BENCHMARK);
      testConfig.role("all", 1);
      testConfig.workload(new EmptyBenchmark(), "all");
      return testConfig;
    });

    File expectedBenchmarkDir = new File(outputDir, SAMPLE_BENCHMARK);
    assertTrue(expectedBenchmarkDir.exists());

    // Node directory name is the role + a number
    File expectedNodeDir = new File(expectedBenchmarkDir, "all-0");
    assertTrue(expectedNodeDir.exists());

    // We expect the node directory to have benchmark results
    Stream<Path> outputFiles = Files.walk(expectedNodeDir.toPath())
        .filter(nameMatches(YardstickThroughputSensorParser.sensorOutputFile));

    assertEquals(1, outputFiles.count());
  }

  @Test
  public void configuresJVMOptions() throws Exception {
    runner.runTest(() -> {
      TestConfig testConfig = new TestConfig();
      testConfig.name(SAMPLE_BENCHMARK);
      testConfig.role("all", 1);
      testConfig.jvmArgs("all", "-Dprop1=true", "-Dprop2=5");
      testConfig.before(context -> {
        assertTrue("Expecting system property to be set in launched JVM, but it was not present.",
            Boolean.getBoolean("prop1"));
        assertEquals("Expecting system property to be set in launched JVM, but it was not present.",
            5, Integer.getInteger("prop2").intValue());
      }, "all");
      return testConfig;
    });
  }

  private Predicate<Path> nameMatches(String sensorOutputFile) {
    return path -> path.toString().contains(sensorOutputFile);
  }
}
