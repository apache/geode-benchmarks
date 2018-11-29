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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.geode.perftest.yardstick.analysis.YardstickHdrHistogramParser;
import org.apache.geode.perftest.yardstick.analysis.YardstickPercentileSensorParser;
import org.apache.geode.perftest.yardstick.analysis.YardstickThroughputSensorParser;

public class Analyzer {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println(
          "Analyzer takes two test output directories as arguments, test results followed by baseline run result.");
      System.exit(1);
      return;
    }

    String testResultArg = args[0];
    String baselineResultArg = args[1];

    File testResultDir = new File(testResultArg);
    File baselineResultDir = new File(baselineResultArg);

    boolean valid = true;
    if (!testResultDir.exists()) {
      System.out.println("Unable to find test result directory: " + testResultArg);
      valid = false;
    }
    if (!baselineResultDir.exists()) {
      System.out.println("Unable to find test result directory: " + baselineResultArg);
      valid = false;
    }
    if (!valid) {
      System.exit(1);
      return;
    }

    System.out.println("Running analyzer");
    System.out.println(
        "Comparing test result at " + testResultArg + " to baseline at " + baselineResultArg);

    BenchmarkRunAnalyzer analyzer = new BenchmarkRunAnalyzer();
    analyzer.addProbe(new YardstickThroughputSensorParser());
    analyzer.addProbe(new YardstickPercentileSensorParser());
    analyzer.addProbe(new YardstickHdrHistogramParser());

    analyzer.analyzeTestRun(testResultDir, baselineResultDir)
        .writeResult(new PrintWriter(System.out));
  }
}
