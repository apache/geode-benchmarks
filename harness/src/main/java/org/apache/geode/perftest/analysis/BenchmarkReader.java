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

import static org.apache.geode.perftest.analysis.BenchmarkRunAnalyzer.getTestResult;
import static org.apache.geode.perftest.analysis.BenchmarkRunAnalyzer.getYardstickOutputForBenchmarkDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BenchmarkReader {
  private final List<ProbeResultParser> probes = new ArrayList<>();

  /**
   * Add a probe to produce a comparison for. The probe expects to find output files
   * in the result directory for each node of each benchmark.
   */
  public void addProbe(ProbeResultParser probeResultParser) {
    probes.add(probeResultParser);
  }

  public Map<String, Map<String, ProbeResultParser.ResultData>> readBenchmarks(File benchmarkDir)
      throws IOException {

    if (benchmarkDir == null || !benchmarkDir.exists()) {
      throw new IllegalStateException("Could not find directory " + benchmarkDir);
    }

    benchmarkDir = findBenchmarkDir(benchmarkDir);

    Map<String, Map<String, ProbeResultParser.ResultData>> results = new HashMap<>();
    List<File> benchmarkDirs = Arrays.asList(benchmarkDir.listFiles());
    benchmarkDirs.sort(File::compareTo);
    for (File testDir : benchmarkDirs) {
      final List<File> yardstickDirs = getYardstickOutputForBenchmarkDir(testDir);
      if (yardstickDirs.isEmpty()) {
        continue;
      }

      for (ProbeResultParser probe : probes) {
        List<ProbeResultParser.ResultData> testResults = getTestResult(yardstickDirs, probe);
        Map<String, ProbeResultParser.ResultData> testResultMap = testResults.stream()
            .collect(Collectors.toMap(ProbeResultParser.ResultData::getDescription,
                Function.identity()));
        results.put(testDir.getName(), testResultMap);
      }
    }

    return results;
  }

  private File findBenchmarkDir(File baseDir) {
    if (!baseDir.getName().startsWith("benchmarks_")) {
      Optional<String> benchmarkSubDir = Arrays.asList(baseDir.list()).stream()
          .filter(subDir -> subDir.startsWith("benchmarks_")).findFirst();
      String subDirName = benchmarkSubDir.orElseThrow(
          () -> new IllegalStateException("No benchmark directory found in " + baseDir));
      return new File(baseDir, subDirName);
    } else {
      return baseDir;
    }
  }

}
