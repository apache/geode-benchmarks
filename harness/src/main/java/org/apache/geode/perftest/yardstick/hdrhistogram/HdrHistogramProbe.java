/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.perftest.yardstick.hdrhistogram;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.HdrHistogram.Histogram;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;
import org.yardstickframework.BenchmarkExecutionAwareProbe;
import org.yardstickframework.BenchmarkProbePoint;
import org.yardstickframework.BenchmarkTotalsOnlyProbe;

/**
 * Probe which returns generates throughput and latency information
 * using HdrHistogram.
 *
 * This probe currently just reports a single summary of the histogram
 * at the end of the test. HdrHistogram actually has a lot of nice support for
 * recording histogram values at time intervals to a file and visualizing the results.
 * See SingleWriterRecorder, HistogramLogWriter, and HisogramLogAnalyzer.
 *
 * TODO consider writing per interval histograms using HistogramLogWriter
 */
public class HdrHistogramProbe implements BenchmarkExecutionAwareProbe, BenchmarkTotalsOnlyProbe {

  private final int lower;
  private final long upper;
  private final int numDigits;
  private final Clock clock;
  private final Consumer<Histogram> histogramConsumer;
  private long[] startTimes;
  private Histogram[] histograms;

  public HdrHistogramProbe(Consumer<Histogram> histogramConsumer) {
    this(1, TimeUnit.HOURS.toNanos(5), 3, () -> System.nanoTime(), histogramConsumer);
  }

  public HdrHistogramProbe(int lower, long upper, int numDigits, Clock clock,
      Consumer<Histogram> histogramConsumer) {
    this.lower = lower;
    this.upper = upper;
    this.numDigits = numDigits;
    this.clock = clock;
    this.histogramConsumer = histogramConsumer;
  }

  @Override
  public void beforeExecute(int threadIdx) {
    startTimes[threadIdx] = clock.currentTimeNanos();
  }

  @Override
  public void afterExecute(int threadIdx) {
    histograms[threadIdx].recordValue(clock.currentTimeNanos() - startTimes[threadIdx]);
  }

  @Override
  public void start(BenchmarkDriver drv, BenchmarkConfiguration cfg) throws Exception {
    int threads = cfg.threads();
    start(threads);

  }

  void start(int threads) {
    startTimes = new long[threads];
    histograms = new Histogram[threads];

    reset();
  }

  private void reset() {
    for (int i = 0; i < histograms.length; i++) {
      histograms[i] = new Histogram(lower, upper, numDigits);
    }
  }

  @Override
  public void stop() {}

  @Override
  public Collection<String> metaInfo() {
    return Arrays.asList("Timestamp", "Mean latency, nanos", "99th percentile latency, nanos");
  }

  @Override
  public Collection<BenchmarkProbePoint> points() {
    Histogram aggregate = getHistogram();
    reset();

    double percentile50 = aggregate.getMean();
    long percentile99 = aggregate.getValueAtPercentile(99);

    BenchmarkProbePoint point =
        new BenchmarkProbePoint(0, new double[] {percentile50, percentile99});

    histogramConsumer.accept(aggregate);
    return Collections.singleton(point);
  }

  @Override
  public void buildPoint(long time) {

  }



  public Histogram getHistogram() {
    Histogram aggregate = new Histogram(lower, upper, numDigits);
    for (Histogram histogram : histograms) {
      aggregate.add(histogram);
    }
    return aggregate;
  }
}
