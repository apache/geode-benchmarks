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
import java.util.Arrays;
import java.util.Map;

import org.apache.geode.perftest.yardstick.analysis.YardstickHdrHistogramParser;

/**
 * Java main method that prints benchmark results for multiple benchmark directories
 * to the terminal.
 */
public class DumpResults {

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println(
          "Usage: dump_results.sh benchmarkDir1 benchmarkDir2 ....");
      System.exit(1);
      return;
    }

    BenchmarkReader reader = new BenchmarkReader();
    reader.addProbe(new YardstickHdrHistogramParser());
    System.out.printf("%-40s %-30s %-16s %-16s %-16s\n", "Directory", "Benchmark", "ops/sec",
        "avg latency(ms)", "99%% latency(ms)");

    Arrays.sort(args);
    for (String directoryName : args) {
      final File benchmarkDir = new File(directoryName);
      Map<String, Map<String, ProbeResultParser.ResultData>> directoryResults =
          reader.readBenchmarks(benchmarkDir);
      for (Map.Entry<String, Map<String, ProbeResultParser.ResultData>> benchmarkResult : directoryResults
          .entrySet()) {

        String name = benchmarkResult.getKey().replaceAll(".*\\.", "");
        double opsPerSec =
            benchmarkResult.getValue().get(YardstickHdrHistogramParser.AVERAGE_OPS_SECOND).value;
        double latency =
            benchmarkResult.getValue().get(YardstickHdrHistogramParser.AVERAGE_LATENCY).value
                / 1_000_000.0;
        double latency_99 =
            benchmarkResult.getValue().get(YardstickHdrHistogramParser.PERCENTILE_LATENCY_99).value
                / 1_000_000.0;

        System.out.printf("%-40s %-30s %-16.2f %-16.4f %-16.4f\n", benchmarkDir.getName(), name,
            opsPerSec, latency, latency_99);
      }


    }
  }
}
