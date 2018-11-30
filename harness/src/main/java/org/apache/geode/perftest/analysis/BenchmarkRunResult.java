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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkRunResult implements Serializable {
  private List<BenchmarkResult> benchmarkResults = new ArrayList<>();

  public BenchmarkResult addBenchmark(String name) {
    final BenchmarkResult benchmarkResult = new BenchmarkResult(name);
    benchmarkResults.add(benchmarkResult);
    return benchmarkResult;
  }

  public void writeResult(Writer output) throws IOException {
    PrintWriter stream = new PrintWriter(output);
    for (BenchmarkResult benchmarkResult : benchmarkResults) {
      stream.println("-- " + benchmarkResult.name + " --");
      for (ProbeResult probeResult : benchmarkResult.probeResults) {
        stream.println(probeResult.description);
        stream.println("Result: " + String.valueOf(probeResult.test));
        stream.println("Baseline: " + String.valueOf(probeResult.baseline));
        stream.println(
            "Relative performance: " + String.valueOf(probeResult.test / probeResult.baseline));
        stream.println();
      }
    }

    output.flush();
  }

  static class BenchmarkResult implements Serializable {
    private final String name;
    private final List<ProbeResult> probeResults = new ArrayList<>();

    public BenchmarkResult(String name) {
      this.name = name;
    }

    public void addProbeResult(String name, double baseline, double test) {
      probeResults.add(new ProbeResult(name, baseline, test));
    }
  }

  private static class ProbeResult implements Serializable {
    private final String description;
    private final double baseline;
    private final double test;

    public ProbeResult(String description, double baseline, double test) {
      this.description = description;
      this.baseline = baseline;
      this.test = test;
    }
  }
}
