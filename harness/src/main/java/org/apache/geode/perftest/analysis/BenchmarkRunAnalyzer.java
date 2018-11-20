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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Analyzer that takes in benchmarks, probes, and result directories and produces
 * a comparison of the results to a provided writer.
 *
 * This currently handles data in the format
 * <pre>
 *   Result1
 *     /BenchmarkA
 *       /client1
 *         /Probe1.csv
 *       /client2
 *         /Probe2.csv
 *     /BenchmarkB
 *         ...
 * </pre>
 */
public class BenchmarkRunAnalyzer {
  private final List<SensorData> benchmarks = new ArrayList<>();
  private final List<ProbeResultParser> probes = new ArrayList<>();

  /**
   * Add a benchmark to be analyzed. The benchmark is expected to exist
   * in both result directories passed to {@link #analyzeTestRun(File, File, Writer)}
   */
  public void addBenchmark(String name, String testResultDir, List<String> nodeNames) {
    benchmarks.add(new SensorData(name, testResultDir, nodeNames));
  }

  /**
   * Add a probe to produce a comparison for. The probe expects to find output files
   * in the result directory for each node of each benchmark.
   */
  public void addProbe(ProbeResultParser probeResultParser) {
    probes.add(probeResultParser);
  }

  public void analyzeTestRun(File testResultDir, File baselineResultDir, Writer output)
      throws IOException {
    PrintWriter stream = new PrintWriter(output);
    for (SensorData benchmark : benchmarks) {
      stream.println("-- " + benchmark.benchmarkName + " --");
      for (ProbeResultParser probe : probes) {
        stream.println(probe.getResultDescription());
        for (String node : benchmark.nodesToParse) {
          probe.parseResults(new File(new File(testResultDir, benchmark.benchmarkSubdirectory), node));
        }
        double testResult = probe.getProbeResult();
        stream.println("Result: " + String.valueOf(testResult));
        probe.reset();
        for (String node : benchmark.nodesToParse) {
          probe.parseResults(new File(new File(baselineResultDir, benchmark.benchmarkSubdirectory), node));
        }
        double baselineResult = probe.getProbeResult();
        stream.println("Baseline: " + String.valueOf(baselineResult));
        stream.println("Relative performance: " + String.valueOf(testResult / baselineResult));
        stream.println();
      }
    }

    stream.flush();
  }

  // TODO: depending on how run output is stored, this data may be excessive or insufficient
  // The present assumption is each benchmark contains an arbitrarily named result directory
  // containing subdirectories for each node.  Those subdirectories then contain the probe output
  // files for the run, for that node.
  private static class SensorData {
    private final String benchmarkName;
    private final String benchmarkSubdirectory;
    private final List<String> nodesToParse;

    public SensorData(String benchmarkName, String benchmarkSubdirectory, List<String> nodesNames) {
      this.benchmarkName = benchmarkName;
      this.benchmarkSubdirectory = benchmarkSubdirectory;
      this.nodesToParse = nodesNames;
    }
  }

}
