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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.perftest.yardstick.analysis.YardstickPercentileSensorParser;
import org.apache.geode.perftest.yardstick.analysis.YardstickThroughputSensorParser;

public class BenchmarkRunAnalyzerTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void verifyResultHarvester() throws IOException {
    final File testFolder = temporaryFolder.newFolder("testFolder");
    final File testBenchmarkA1 = temporaryFolder.newFolder("testFolder", "BenchmarkA", "client1",
        "20181121-111516-yardstick-output");
    final File testBenchmarkA2 = temporaryFolder.newFolder("testFolder", "BenchmarkA", "client2",
        "20181121-111516-yardstick-output");
    final File testBenchmarkB1 = temporaryFolder.newFolder("testFolder", "BenchmarkB", "client1",
        "20181121-111516-yardstick-output");
    final File testBenchmarkB2 = temporaryFolder.newFolder("testFolder", "BenchmarkB", "client2",
        "20181121-111516-yardstick-output");
    temporaryFolder.newFolder(testFolder.getName(), "junkfolder");
    new File(testFolder, "junkfile").createNewFile();
    final File baseFolder = temporaryFolder.newFolder("baseFolder");
    final File baseBenchmarkA1 = temporaryFolder.newFolder("baseFolder", "BenchmarkA", "client1",
        "20181121-111516-yardstick-output");
    final File baseBenchmarkA2 = temporaryFolder.newFolder("baseFolder", "BenchmarkA", "client2",
        "20181121-111516-yardstick-output");
    final File baseBenchmarkB1 = temporaryFolder.newFolder("baseFolder", "BenchmarkB", "client1",
        "20181121-111516-yardstick-output");
    final File baseBenchmarkB2 = temporaryFolder.newFolder("baseFolder", "BenchmarkB", "client2",
        "20181121-111516-yardstick-output");

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

    BenchmarkRunResult results = harvester.analyzeTestRun(testFolder, baseFolder);

    BenchmarkRunResult expectedBenchmarkResult = new BenchmarkRunResult();
    BenchmarkRunResult.BenchmarkResult resultA = expectedBenchmarkResult.addBenchmark("BenchmarkA");
    resultA.addProbeResult(YardstickThroughputSensorParser.probeResultDescription, 25, 20);
    resultA.addProbeResult(YardstickPercentileSensorParser.probeResultDescription, 200, 300);
    BenchmarkRunResult.BenchmarkResult resultB = expectedBenchmarkResult.addBenchmark("BenchmarkB");
    resultB.addProbeResult(YardstickThroughputSensorParser.probeResultDescription, 20, 25);
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
