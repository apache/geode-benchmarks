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
package org.apache.geode.perftest.yardstick.hdrhistogram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;

public class HdrHistogramWriter implements Consumer<Histogram> {

  public static final String FILE_NAME = "latency.hlog";
  private final File outputFile;

  public HdrHistogramWriter(File outputDir) {
    this.outputFile = new File(outputDir, FILE_NAME);
  }

  @Override
  public void accept(Histogram histogram) {

    try {
      HistogramLogWriter writer = new HistogramLogWriter(outputFile);
      try {
        writer.outputIntervalHistogram(histogram);
      } finally {
        writer.close();
      }
    } catch (FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
  }
}
