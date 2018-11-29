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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.function.Consumer;

import org.HdrHistogram.Histogram;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.yardstickframework.BenchmarkProbePoint;

public class HdrHistogramProbeTest {

  private HdrHistogramProbe probe;
  private Clock clock;
  private Consumer consumer;

  @Before
  public void setUp() {
    clock = mock(Clock.class);
    consumer = mock(Consumer.class);
    probe = new HdrHistogramProbe(1, 3_600_000, 3, clock, consumer);
  }

  @Test
  public void recordsASingleValue() throws InterruptedException {
    probe.start(8);
    when(clock.currentTimeNanos()).thenReturn(0L);
    probe.beforeExecute(1);
    when(clock.currentTimeNanos()).thenReturn(2L);
    probe.afterExecute(1);

    Histogram histogram = probe.getHistogram();
    assertEquals(2, histogram.getMaxValue());
    System.out.println("Size=" + histogram.getEstimatedFootprintInBytes());
  }

  @Test
  public void recordsFromMultipleThreads() {
    probe.start(3);

    when(clock.currentTimeNanos()).thenReturn(0L);
    probe.beforeExecute(0);

    when(clock.currentTimeNanos()).thenReturn(1L);
    probe.beforeExecute(2);

    when(clock.currentTimeNanos()).thenReturn(1L);
    probe.beforeExecute(1);

    when(clock.currentTimeNanos()).thenReturn(3L);
    probe.afterExecute(2);

    when(clock.currentTimeNanos()).thenReturn(3L);
    probe.afterExecute(1);

    when(clock.currentTimeNanos()).thenReturn(4L);
    probe.afterExecute(0);

    assertEquals(4, probe.getHistogram().getMaxValue());
    assertEquals(8.0 / 3.0, probe.getHistogram().getMean(), 0.01);
  }

  @Test
  public void generatesASummaryBenchmarkPoint() throws Exception {
    probe.start(1);
    when(clock.currentTimeNanos()).thenReturn(0L);
    probe.beforeExecute(0);
    when(clock.currentTimeNanos()).thenReturn(2L);
    probe.afterExecute(0);
    probe.beforeExecute(0);
    when(clock.currentTimeNanos()).thenReturn(6L);
    probe.afterExecute(0);
    when(clock.currentTimeNanos()).thenReturn(8L);

    probe.stop();

    Collection<BenchmarkProbePoint> points = probe.points();
    assertEquals(1, points.size());
    BenchmarkProbePoint point = points.iterator().next();

    assertEquals(0, point.time());
    Assertions.assertThat(point.values()).containsExactly(3.0, 4.0);
  }
}
