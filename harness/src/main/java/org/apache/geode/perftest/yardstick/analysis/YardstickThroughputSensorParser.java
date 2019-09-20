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
package org.apache.geode.perftest.yardstick.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yardstickframework.probes.ThroughputLatencyProbe;

import org.apache.geode.perftest.analysis.ProbeResultParser;

/**
 * Parses the results from a {@link ThroughputLatencyProbe} and
 * reports the average throughput in operations/second.
 */
public class YardstickThroughputSensorParser implements ProbeResultParser {
  private static final Logger logger =
      LoggerFactory.getLogger(YardstickThroughputSensorParser.class);
  public static final String sensorOutputFile = "ThroughputLatencyProbe.csv";
  public static final String probeResultDescription = "average ops/second";

  private List<SensorDatapoint> datapoints = new ArrayList<>();

  public void parseResults(File resultDir) throws IOException {
    File sensorData = new File(resultDir, sensorOutputFile);
    try (BufferedReader dataStream = new BufferedReader(new FileReader(sensorData))) {
      String nextLine;

      while ((nextLine = dataStream.readLine()) != null) {
        if (nextLine.startsWith("--") ||
            nextLine.startsWith("@@") ||
            nextLine.startsWith("**")) {
          continue;
        }
        datapoints.add(new SensorDatapoint(nextLine));
      }
    } catch (java.io.FileNotFoundException e) {
      logger.warn("Result file {} missing.", sensorData, e);
    }
  }

  @Override
  public void reset() {
    datapoints = new ArrayList<>();
  }

  @Override
  public List<ResultData> getProbeResults() {
    List<ResultData> results = new ArrayList<>(1);
    double averageThroughput = getAverageThroughput();
    double standardDeviation = getStandardDeviation(averageThroughput);
    double standardError = standardDeviation / Math.sqrt(datapoints.size());
    results.add(new ResultData(probeResultDescription, averageThroughput));
    results.add(new ResultData("ops/second standard error", standardError));
    results.add(new ResultData("ops/second standard deviation", standardDeviation));

    return results;
  }

  public double getAverageThroughput() {
    double accumulator = 0;
    for (SensorDatapoint datapoint : datapoints) {
      accumulator += datapoint.opsPerSec;
    }
    return accumulator / datapoints.size();
  }

  public double getStandardDeviation(double average) {
    double accumulator = 0;
    for (SensorDatapoint datapoint : datapoints) {
      double deviation = datapoint.opsPerSec - average;
      accumulator += deviation * deviation;
    }
    return Math.sqrt(accumulator / (datapoints.size() - 1));
  }

  private static class SensorDatapoint {
    private int second;
    private double opsPerSec;
    private double avgLatency;

    SensorDatapoint(String dataLine) throws IOException {
      String[] data = dataLine.split(",");
      if (data.length != 3) {
        throw new IOException("Invalid data line: " + dataLine);
      }
      try {
        second = Integer.parseInt(data[0]);
        opsPerSec = Float.parseFloat(data[1]);
        avgLatency = Float.parseFloat(data[2]);
      } catch (NumberFormatException e) {
        throw new IOException("Invalid data line: " + dataLine);
      }
    }
  }
}
