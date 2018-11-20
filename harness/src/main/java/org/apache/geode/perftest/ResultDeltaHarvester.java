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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ResultDeltaHarvester {
  // TODO: depending on how run output is stored, this data may be excessive or insufficient
  // The present assumption is each benchmark contains an arbitrarily named result directory
  // containing subdirectories for each node.  Those subdirectories then contain the probe output
  // files for the run, for that node.
  public static class SensorData {
    String benchmarkName;
    String benchmarkSubdirectory;
    List<String> nodesToParse;

    public SensorData(String benchmarkName, String benchmarkSubdirectory, List<String> nodesNames) {
      this.benchmarkName = benchmarkName;
      this.benchmarkSubdirectory = benchmarkSubdirectory;
      this.nodesToParse = nodesNames;
    }
  }

  private List<SensorData> benchmarks = new ArrayList<>();
  private List<ProbeResultParser> probes = new ArrayList<>();

  public void addBenchmark(String name, String testResultDir, List<String> nodeNames) {
    benchmarks.add(new SensorData(name, testResultDir, nodeNames));
  }

  public void addProbe(ProbeResultParser probeResultParser) {
    probes.add(probeResultParser);
  }

  public void harvestResults(File testResultDir, File baselineResultDir, OutputStream output)
      throws IOException {
    PrintStream stream = new PrintStream(output);
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
}
