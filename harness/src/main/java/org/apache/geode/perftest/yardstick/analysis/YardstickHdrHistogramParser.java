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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.yardstickframework.probes.PercentileProbe;

import org.apache.geode.perftest.analysis.ProbeResultParser;
import org.apache.geode.perftest.yardstick.hdrhistogram.HdrHistogramWriter;

/**
 * Parses the output from {@link PercentileProbe} and reports the
 * 99% percentile latency in microseconds.
 */
public class YardstickHdrHistogramParser implements ProbeResultParser {
  public static final String sensorOutputFile = HdrHistogramWriter.FILE_NAME;
  public static final String AVERAGE_OPS_SECOND = "average ops/second";
  public static final String AVERAGE_LATENCY = "average latency";
  public static final String PERCENTILE_LATENCY_99 = "99th percentile latency";

  public Histogram histogram = null;

  public void parseResults(File resultDir) throws IOException {
    File sensorData = new File(resultDir.getParent(), sensorOutputFile);

    HistogramLogReader reader = new HistogramLogReader(sensorData);

    final Histogram nextIntervalHistogram = (Histogram) reader.nextIntervalHistogram();

    if (histogram == null) {
      histogram = nextIntervalHistogram;
    } else {
      histogram.add(nextIntervalHistogram);
    }
  }

  @Override
  public void reset() {
    histogram = null;
  }

  @Override
  public List<ResultData> getProbeResults() {
    List<ResultData> results = new ArrayList<>(3);
    results.add(new ResultData("median latency", histogram.getValueAtPercentile(50)));
    results.add(new ResultData("90th percentile latency", histogram.getValueAtPercentile(90)));
    results.add(new ResultData(PERCENTILE_LATENCY_99, histogram.getValueAtPercentile(99)));
    results.add(new ResultData("99.9th percentile latency", histogram.getValueAtPercentile(99.9)));
    results.add(new ResultData(AVERAGE_LATENCY, histogram.getMean()));
    results.add(new ResultData("latency standard deviation", histogram.getStdDeviation()));
    results.add(new ResultData("latency standard error",
        histogram.getStdDeviation() / Math.sqrt(histogram.getTotalCount())));
    results.add(new ResultData(AVERAGE_OPS_SECOND, ((double) histogram.getTotalCount())
        / (histogram.getEndTimeStamp() - histogram.getStartTimeStamp()) * 1000));
    return results;
  }
}
