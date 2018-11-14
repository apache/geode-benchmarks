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
package org.apache.geode.perftest.yardstick;

import static java.lang.Math.abs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class YardstickPercentileSensorParser {
  static final String sensorOutputFile = "PercentileProbe.csv";

  private class SensorBucket {
    public int latencyBucket;
    public float bucketPercentage;

    SensorBucket(String dataLine) throws IOException {
      String[] data = dataLine.split(",");
      if (data.length != 2) {
        throw new IOException("Invalid data line: " + dataLine);
      }
      try {
        latencyBucket = Integer.parseInt(data[0]);
        bucketPercentage = Float.parseFloat(data[1]);
      } catch (NumberFormatException e) {
        throw new IOException("Invalid data line: " + dataLine);
      }
    }
  }

  private ArrayList<SensorBucket> buckets = new ArrayList<>();

  public void parseResults(File resultDir) throws IOException {
    File sensorData = new File(resultDir, sensorOutputFile);
    BufferedReader dataStream = new BufferedReader(new FileReader(sensorData));
    String nextLine;

    while ((nextLine = dataStream.readLine()) != null) {
      if (nextLine.startsWith("--") ||
          nextLine.startsWith("@@") ||
          nextLine.startsWith("**")) {
        continue;
      }
      buckets.add(new SensorBucket(nextLine));
    }
  }

  private void normalizeBuckets() {
    float totalPercentage = 0;
    for (SensorBucket bucket : buckets) {
      totalPercentage += bucket.bucketPercentage;
    }

    if (abs(1.0 - totalPercentage) > 0.0001) {
      for (SensorBucket bucket : buckets) {
        bucket.bucketPercentage /= totalPercentage;
      }
    }
  }

  public float getPercentile(int target) {
    if (target < 0 || target > 100) {
      throw new RuntimeException("Percentile must be in the range (0, 100), invalid value: " + target);
    }
    float targetPercent = target / 100f;
    normalizeBuckets();

    if (buckets.size() == 1) {
      return buckets.get(0).latencyBucket;  // Just one bucket doesn't give us much info
    }

    SensorBucket[] bucketArray = buckets.toArray(new SensorBucket[buckets.size()]);

    float accumulator = 0;
    int i = -1;
    while (targetPercent - accumulator > 0.0001) {
      ++i;
      accumulator += bucketArray[i].bucketPercentage;
    }
    // Post-condition: accumulator >= targetPercent, i is index of last block added

    SensorBucket targetBucket = bucketArray[i];
    // If last bucket contains the target percentile, assume bucket size is same as previous bucket
    int bucketSize = (bucketArray.length > i + 1) ?
        bucketArray[i + 1].latencyBucket - targetBucket.latencyBucket :
        targetBucket.latencyBucket - bucketArray[i - 1].latencyBucket;

    float percentileLocationInTargetBucket = 1.0f - ((accumulator - targetPercent) / targetBucket.bucketPercentage);

    return targetBucket.latencyBucket + bucketSize * percentileLocationInTargetBucket;
  }
}
