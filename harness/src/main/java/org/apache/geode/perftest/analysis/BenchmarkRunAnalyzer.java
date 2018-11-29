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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.geode.perftest.yardstick.YardstickTask;

/**
 * Analyzer that takes in benchmarks, probes, and result directories and produces
 * a comparison of the results to a provided writer.
 *
 * This currently handles data in the format
 *
 * <pre>
 *   Result1
 *     /BenchmarkA
 *       /client1
 *         /20181121-111354-yardstick-output
 *           /Probe1.csv
 *       /client2
 *         /20181121-111354-yardstick-output
 *           /Probe1.csv
 *     /BenchmarkB
 *         ...
 * </pre>
 */
public class BenchmarkRunAnalyzer {
  private final List<ProbeResultParser> probes = new ArrayList<>();

  /**
   * Add a probe to produce a comparison for. The probe expects to find output files
   * in the result directory for each node of each benchmark.
   */
  public void addProbe(ProbeResultParser probeResultParser) {
    probes.add(probeResultParser);
  }

  public BenchmarkRunResult analyzeTestRun(File testResultDir, File baselineResultDir)
      throws IOException {
    List<File> benchmarkDirs = Arrays.asList(testResultDir.listFiles());
    benchmarkDirs.sort(File::compareTo);
    BenchmarkRunResult result = new BenchmarkRunResult();

    for (File testDir : benchmarkDirs) {
      final List<File> testYardstickDirs = getYardstickOutputForBenchmarkDir(testDir);
      if (testYardstickDirs.isEmpty()) {
        continue;
      }
      File baselineDir = new File(baselineResultDir, testDir.getName());
      final List<File> baselineYardstickDirs = getYardstickOutputForBenchmarkDir(baselineDir);

      final BenchmarkRunResult.BenchmarkResult benchmarkResult =
          result.addBenchmark(testDir.getName());
      for (ProbeResultParser probe : probes) {
        double testResult = getTestResult(testYardstickDirs, probe);
        double baselineResult = getTestResult(baselineYardstickDirs, probe);

        benchmarkResult.addProbeResult(probe.getResultDescription(), baselineResult, testResult);
      }
    }

    return result;
  }

  private double getTestResult(List<File> resultDirs, ProbeResultParser probe) throws IOException {
    probe.reset();
    for (File outputDirectory : resultDirs) {
      probe.parseResults(outputDirectory);
    }
    return probe.getProbeResult();
  }

  private List<File> getYardstickOutputForBenchmarkDir(File benchmarkDir) throws IOException {
    return Files.walk(benchmarkDir.toPath(), 2)
        .filter(path -> path.toString().endsWith(YardstickTask.YARDSTICK_OUTPUT))
        .map(Path::toFile)
        .collect(Collectors.toList());
  }
}
