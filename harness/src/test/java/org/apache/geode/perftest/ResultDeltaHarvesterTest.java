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

package org.apache.geode.perftest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.perftest.yardstick.YardstickPercentileSensorParser;
import org.apache.geode.perftest.yardstick.YardstickThroughputSensorParser;

public class ResultDeltaHarvesterTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void verifyResultHarvester() throws IOException {
    final File testFolder = temporaryFolder.newFolder("testFolder");
    final File testBenchmarkA = temporaryFolder.newFolder("testFolder","BenchmarkA");
    final File testBenchmarkB = temporaryFolder.newFolder("testFolder","BenchmarkB");
    final File baseFolder = temporaryFolder.newFolder("baseFolder");
    final File baseBenchmarkA = temporaryFolder.newFolder("baseFolder","BenchmarkA");
    final File baseBenchmarkB = temporaryFolder.newFolder("baseFolder","BenchmarkB");

    populateThroughputCSV(testBenchmarkA, new double[] {10, 15, 20, 25, 30});  // Avg 20
    populatePercentileCSV(testBenchmarkA, new double[] {0, 0, 99, 1});         // 200
    populateThroughputCSV(testBenchmarkB, new double[] {10, 15, 20, 25, 30, 35, 40});  // Avg 25
    populatePercentileCSV(testBenchmarkB, new double[] {0, 0, 0, 99, 1});      // 300
    populateThroughputCSV(baseBenchmarkA, new double[] {15, 20, 25, 30, 35});  // Avg 25
    populatePercentileCSV(baseBenchmarkA, new double[] {0, 99, 1});         // 100
    populateThroughputCSV(baseBenchmarkB, new double[] {10, 15, 20, 25, 30});  // Avg 20
    populatePercentileCSV(baseBenchmarkB, new double[] {0, 0, 0, 99, 1});      // 300

    ResultDeltaHarvester harvester = new ResultDeltaHarvester();
    harvester.addBenchmark("Alpha", "BenchmarkA");
    harvester.addBenchmark("Beta", "BenchmarkB");
    harvester.addProbe(new YardstickThroughputSensorParser());
    harvester.addProbe(new YardstickPercentileSensorParser());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4000);
    harvester.harvestResults(testFolder, baseFolder, outputStream);
    BufferedReader resultReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));

    validatedBenchmark(resultReader, "Alpha", 20, 300, 25, 200);
    validatedBenchmark(resultReader, "Beta", 25, 400, 20, 400);
  }

  private void populateThroughputCSV(File targetDirectory, double[] perSecondThroughputs)
      throws FileNotFoundException {
    final File testFile = new File(targetDirectory, YardstickThroughputSensorParser.sensorOutputFile);
    PrintStream output = new PrintStream(testFile);
    for (int i = 0; i < perSecondThroughputs.length; ++i) {
      output.println(String.format("%d,%f,%f", i, perSecondThroughputs[i], 1d));
    }
    output.close();
  }

  private void populatePercentileCSV(File targetDirectory, double[] hundredUsBuckets)
      throws FileNotFoundException {
    final File testFile = new File(targetDirectory, YardstickPercentileSensorParser.sensorOutputFile);
    PrintStream output = new PrintStream(testFile);
    for (int i = 0; i < hundredUsBuckets.length; ++i) {
      output.println(String.format("%d,%f", 100*i, hundredUsBuckets[i]));
    }
    output.close();
  }

  private void validatedBenchmark(BufferedReader input, String benchmarkName, double testValA, double testValB, double baseValA, double baseValB)
      throws IOException {
    String line = input.readLine();
    Assert.assertEquals("-- " + benchmarkName + " --", line);
    validateProbe(input, YardstickThroughputSensorParser.probeResultDescription, testValA, baseValA);
    validateProbe(input, YardstickPercentileSensorParser.probeResultDescription, testValB, baseValB);
  }

  private void validateProbe(BufferedReader input, String description, double testVal, double baseVal)
      throws IOException {
    String line = input.readLine();
    Assert.assertEquals(description, line);
    Scanner scanner = new Scanner(input.readLine());
    while (!scanner.hasNextDouble()) {scanner.next();}
    Assert.assertEquals(testVal, scanner.nextDouble(), 0.1);
    scanner = new Scanner(input.readLine());
    while (!scanner.hasNextDouble()) {scanner.next();}
    Assert.assertEquals(baseVal, scanner.nextDouble(), 0.1);
    scanner = new Scanner(input.readLine());
    while (!scanner.hasNextDouble()) {scanner.next();}
    Assert.assertEquals(testVal / baseVal, scanner.nextDouble(), 0.1);
    line = input.readLine();
    Assert.assertEquals("", line);
  }
}