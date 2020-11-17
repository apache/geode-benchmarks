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

package org.apache.geode.perftest.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.apache.geode.perftest.yardstick.analysis.YardstickPercentileSensorParser;
import org.apache.geode.perftest.yardstick.analysis.YardstickThroughputSensorParser;

public class BenchmarkRunAnalyzerTest {

  @TempDir
  Path temporaryFolder;

  @Test
  public void verifyResultHarvester() throws IOException {
    final File testFolder = temporaryFolder.resolve("testFolder").toFile();
    final File testBenchmarkA1 = temporaryFolder
        .resolve("testFolder")
        .resolve("BenchmarkA")
        .resolve("client1")
        .resolve("20181121-111516-yardstick-output")
        .toFile();
    assertTrue(testBenchmarkA1.mkdirs());
    final File testBenchmarkA2 = temporaryFolder
        .resolve("testFolder")
        .resolve("BenchmarkA")
        .resolve("client2")
        .resolve("20181121-111516-yardstick-output")
        .toFile();
    assertTrue(testBenchmarkA2.mkdirs());
    final File testBenchmarkB1 = temporaryFolder
        .resolve("testFolder")
        .resolve("BenchmarkB")
        .resolve("client1")
        .resolve("20181121-111516-yardstick-output")
        .toFile();
    assertTrue(testBenchmarkB1.mkdirs());
    final File testBenchmarkB2 = temporaryFolder
        .resolve("testFolder")
        .resolve("BenchmarkB")
        .resolve("client2")
        .resolve("20181121-111516-yardstick-output")
        .toFile();
    assertTrue(testBenchmarkB2.mkdirs());
    assertTrue(testFolder.toPath().resolve("junkfolder").toFile().mkdirs());
    assertTrue(new File(testFolder, "junkfile").createNewFile());
    final File baseFolder = temporaryFolder.resolve("baseFolder").toFile();
    assertTrue(baseFolder.mkdirs());
    final File baseBenchmarkA1 = temporaryFolder
        .resolve("baseFolder")
        .resolve("BenchmarkA")
        .resolve("client1")
        .resolve("20181121-111516-yardstick-output")
        .toFile();
    assertTrue(baseBenchmarkA1.mkdirs());
    final File baseBenchmarkA2 = temporaryFolder
        .resolve("baseFolder")
        .resolve("BenchmarkA")
        .resolve("client2")
        .resolve("20181121-111516-yardstick-output")
        .toFile();
    assertTrue(baseBenchmarkA2.mkdirs());
    final File baseBenchmarkB1 = temporaryFolder
        .resolve("baseFolder")
        .resolve("BenchmarkB")
        .resolve("client1")
        .resolve("20181121-111516-yardstick-output")
        .toFile();
    assertTrue(baseBenchmarkB1.mkdirs());
    final File baseBenchmarkB2 = temporaryFolder
        .resolve("baseFolder")
        .resolve("BenchmarkB")
        .resolve("client2")
        .resolve("20181121-111516-yardstick-output")
        .toFile();
    assertTrue(baseBenchmarkB2.mkdirs());

    populateThroughputCSV(testBenchmarkA1, new double[] {10, 15, 20, 25, 30}); // Avg 20
    populatePercentileCSV(testBenchmarkA1, new double[] {0, 0, 99, 1}); // 200
    populateThroughputCSV(testBenchmarkB1, new double[] {10, 15, 20, 25, 30, 35, 40}); // Avg 25
    populatePercentileCSV(testBenchmarkB1, new double[] {0, 0, 0, 99, 1}); // 300
    populateThroughputCSV(baseBenchmarkA1, new double[] {15, 20, 25, 30, 35}); // Avg 25
    populatePercentileCSV(baseBenchmarkA1, new double[] {0, 99, 1}); // 100
    populateThroughputCSV(baseBenchmarkB1, new double[] {10, 15, 20, 25, 30}); // Avg 20
    populatePercentileCSV(baseBenchmarkB1, new double[] {0, 0, 0, 99, 1}); // 300

    populateThroughputCSV(testBenchmarkA2, new double[] {10, 15, 20, 25, 30}); // Avg 20
    populatePercentileCSV(testBenchmarkA2, new double[] {0, 0, 99, 1}); // 200
    populateThroughputCSV(testBenchmarkB2, new double[] {10, 15, 20, 25, 30, 35, 40}); // Avg 25
    populatePercentileCSV(testBenchmarkB2, new double[] {0, 0, 0, 99, 1}); // 300
    populateThroughputCSV(baseBenchmarkA2, new double[] {15, 20, 25, 30, 35}); // Avg 25
    populatePercentileCSV(baseBenchmarkA2, new double[] {0, 99, 1}); // 100
    populateThroughputCSV(baseBenchmarkB2, new double[] {10, 15, 20, 25, 30}); // Avg 20
    populatePercentileCSV(baseBenchmarkB2, new double[] {0, 0, 0, 99, 1}); // 300

    BenchmarkRunAnalyzer harvester = new BenchmarkRunAnalyzer();
    harvester.addProbe(new YardstickThroughputSensorParser());
    harvester.addProbe(new YardstickPercentileSensorParser());

    StringWriter writer = new StringWriter();

    BenchmarkRunResult results = harvester.analyzeTestRun(baseFolder, testFolder);

    BenchmarkRunResult expectedBenchmarkResult = new BenchmarkRunResult();
    BenchmarkRunResult.BenchmarkResult resultA = expectedBenchmarkResult.addBenchmark("BenchmarkA");
    resultA.addProbeResult(YardstickThroughputSensorParser.probeResultDescription, 25, 20);
    resultA.addProbeResult("ops/second standard error", 2.36, 2.36);
    resultA.addProbeResult("ops/second standard deviation", 7.45, 7.45);
    resultA.addProbeResult(YardstickPercentileSensorParser.probeResultDescription, 200, 300);
    BenchmarkRunResult.BenchmarkResult resultB = expectedBenchmarkResult.addBenchmark("BenchmarkB");
    resultB.addProbeResult(YardstickThroughputSensorParser.probeResultDescription, 20, 25);
    resultB.addProbeResult("ops/second standard error", 2.36, 2.77);
    resultB.addProbeResult("ops/second standard deviation", 7.45, 10.38);
    resultB.addProbeResult(YardstickPercentileSensorParser.probeResultDescription, 400, 400);
    assertEquals(expectedBenchmarkResult, results);
  }

  private void populateThroughputCSV(File targetDirectory, double[] perSecondThroughputs)
      throws FileNotFoundException {
    final File testFile =
        new File(targetDirectory, YardstickThroughputSensorParser.sensorOutputFile);
    PrintStream output = new PrintStream(testFile);
    for (int i = 0; i < perSecondThroughputs.length; ++i) {
      output.println(String.format("%d,%f,%f", i, perSecondThroughputs[i], 1d));
    }
    output.close();
  }

  private void populatePercentileCSV(File targetDirectory, double[] hundredUsBuckets)
      throws FileNotFoundException {
    final File testFile =
        new File(targetDirectory, YardstickPercentileSensorParser.sensorOutputFile);
    PrintStream output = new PrintStream(testFile);
    for (int i = 0; i < hundredUsBuckets.length; ++i) {
      output.println(String.format("%d,%f", 100 * i, hundredUsBuckets[i]));
    }
    output.close();
  }
}
