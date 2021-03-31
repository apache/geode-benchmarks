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


import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger logger = LoggerFactory.getLogger(HdrHistogramProbe.class);

  private final int lower;
  private long upper;
  private final int numDigits;
  private final Clock clock;
  private final Consumer<Histogram> histogramConsumer;
  private long[] startTimes;
  private Histogram[] histograms;
  private AtomicBoolean warmUpFinished = new AtomicBoolean(false);

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
    final int threads = cfg.threads();
    upper = SECONDS.toNanos(cfg.duration());
    start(threads);

  }

  void start(int threads) {
    startTimes = new long[threads];
    histograms = new Histogram[threads];

    reset();
  }

  private void reset() {
    final long timeStampMsec = System.currentTimeMillis();
    for (int i = 0; i < histograms.length; i++) {
      histograms[i] = new Histogram(lower, upper, numDigits);
      histograms[i].setStartTimeStamp(timeStampMsec);
    }
  }

  @Override
  public void stop() {
    logger.info("Stopped");
  }

  @Override
  public Collection<String> metaInfo() {
    return Arrays.asList("Timestamp", "Mean latency, nanos", "99th percentile latency, nanos");
  }

  @Override
  public Collection<BenchmarkProbePoint> points() {
    final Histogram aggregate = getHistogram();
    if (warmUpFinished.compareAndSet(false, true)) {
      reset();
    }

    final double mean = aggregate.getMean();
    final long percentile99 = aggregate.getValueAtPercentile(99);

    final BenchmarkProbePoint point =
        new BenchmarkProbePoint(aggregate.getEndTimeStamp(), new double[] {mean, percentile99});

    for (int r = 0; r < 5; ++r) {
      try {
        logger.info("Saving histogram. r={}", r);
        histogramConsumer.accept(aggregate);
      } catch (Exception e) {
        logger.error("Failed to save histogram. aggregate={}", aggregate.getTag(), e);
        try {
          Thread.sleep(SECONDS.toMillis(1));
        } catch (InterruptedException ignored) {
        }
        continue;
      }
      break;
    }
    return Collections.singleton(point);
  }

  @Override
  public void buildPoint(long time) {

  }

  public Histogram getHistogram() {
    final Histogram aggregate = new Histogram(lower, upper, numDigits);
    for (final Histogram histogram : histograms) {
      aggregate.add(histogram);
    }
    aggregate.setEndTimeStamp(System.currentTimeMillis());
    return aggregate;
  }

  public boolean isWarmupFinished() {
    return warmUpFinished.get();
  }
}
