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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BenchmarkRunResult implements Serializable {
  private final List<BenchmarkResult> benchmarkResults = new ArrayList<>();

  public BenchmarkResult addBenchmark(String name) {
    final BenchmarkResult benchmarkResult = new BenchmarkResult(name);
    benchmarkResults.add(benchmarkResult);
    return benchmarkResult;
  }

  public void writeResult(Writer output) throws IOException {
    PrintWriter stream = new PrintWriter(output);
    for (BenchmarkResult benchmarkResult : benchmarkResults) {
      stream.println(benchmarkResult.name);
      for (ProbeResult probeResult : benchmarkResult.probeResults) {
        stream.print(String.format("  %30s", probeResult.description));
        stream.print(String.format("  Baseline: %12.2f", probeResult.baseline));
        stream.print(String.format("  Test: %12.2f", probeResult.test));
        stream.print(String.format("  Difference: %+6.1f%%", probeResult.getDifference() * 100));
        stream.println();
      }
    }

    output.flush();
  }

  @Override
  public String toString() {
    StringWriter writer = new StringWriter();
    try {
      this.writeResult(writer);
    } catch (IOException e) {
      throw new IllegalStateException();
    }

    return writer.toString();

  }

  public List<BenchmarkResult> getBenchmarkResults() {
    return benchmarkResults;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BenchmarkRunResult that = (BenchmarkRunResult) o;
    return Objects.equals(benchmarkResults, that.benchmarkResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(benchmarkResults);
  }

  static class BenchmarkResult implements Serializable {
    final String name;
    final List<ProbeResult> probeResults = new ArrayList<>();

    public BenchmarkResult(String name) {
      this.name = name;
    }

    public void addProbeResult(String name, double baseline, double test) {
      probeResults.add(new ProbeResult(name, baseline, test));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      BenchmarkResult that = (BenchmarkResult) o;
      return Objects.equals(name, that.name) &&
          Objects.equals(probeResults, that.probeResults);
    }

    @Override
    public int hashCode() {

      return Objects.hash(name, probeResults);
    }
  }

  static class ProbeResult implements Serializable {
    final String description;
    final double baseline;
    final double test;

    public ProbeResult(String description, double baseline, double test) {
      this.description = description;
      this.baseline = baseline;
      this.test = test;
    }

    public double getDifference() {
      return (test - baseline) / baseline;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ProbeResult that = (ProbeResult) o;
      return fuzzyEquals(that.baseline, baseline) &&
          fuzzyEquals(that.test, test) &&
          Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {

      return Objects.hash(description, baseline, test);
    }

    public boolean fuzzyEquals(double a, double b) {
      double ratio = Math.abs(a / b);
      return ratio < 1.05 && ratio > 0.95;
    }
  }
}
