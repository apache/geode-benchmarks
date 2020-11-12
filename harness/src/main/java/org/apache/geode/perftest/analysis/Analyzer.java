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

import static java.lang.Double.isNaN;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

    String baselineResultArg = args[0];
    String testResultArg = args[1];

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

    boolean isCI = System.getProperty("TEST_CI", "0").equals("1");

    System.out.println("Running analyzer");
    System.out.println(
        "Comparing test result at " + testResultArg + " to baseline at " + baselineResultArg);

    BenchmarkRunAnalyzer analyzer = new BenchmarkRunAnalyzer();
    analyzer.addProbe(new YardstickThroughputSensorParser());
    analyzer.addProbe(new YardstickPercentileSensorParser());
    analyzer.addProbe(new YardstickHdrHistogramParser());

    BenchmarkRunResult benchmarkRunResult =
        analyzer.analyzeTestRun(baselineResultDir, testResultDir);
    benchmarkRunResult.writeResult(new PrintWriter(System.out));
    /* throw exc if failed? */

    String errorFilePath = testResultArg + "/../../../failedTests";
    BufferedWriter writer = new BufferedWriter(new FileWriter(errorFilePath, true));

    boolean isSignificantlyBetter = false;
    boolean isHighWaterCandidate = true;
    StringBuilder errorMessage = new StringBuilder();
    for (BenchmarkRunResult.BenchmarkResult benchmarkResult : benchmarkRunResult
        .getBenchmarkResults()) {
      for (BenchmarkRunResult.ProbeResult probeResult : benchmarkResult.probeResults) {
        if (isNaN(probeResult.baseline) || isNaN(probeResult.test)) {
          errorMessage.append("BENCHMARK FAILED: ").append(benchmarkResult.name)
              .append(" missing result file.\n");
          writer.append(benchmarkResult.name + "\n");
        } else if (probeResult.description.equals("average latency")) {
          if (probeResult.getDifference() > 0) {
            isHighWaterCandidate = false;
            if (probeResult.getDifference() >= 0.05) {
              errorMessage.append("BENCHMARK FAILED: ").append(benchmarkResult.name)
                  .append(" average latency is 5% worse than baseline.\n");
              writer.append(benchmarkResult.name + "\n");
            }
          } else if (probeResult.getDifference() <= -0.5) {
            isSignificantlyBetter = true;
          }
        }
      }
    }
    writer.close();

    if (isCI && isHighWaterCandidate && isSignificantlyBetter) {
      System.out.println(
          "NEW HIGH WATERMARK COMMIT: average latency for each test is <=0.0% change from baseline AND at least one test shows a >=5% improvement in performance.\n");
    }

    if (errorMessage.length() > 0) {
      System.out.println(errorMessage);
      System.exit(1);
    }

  }
}
