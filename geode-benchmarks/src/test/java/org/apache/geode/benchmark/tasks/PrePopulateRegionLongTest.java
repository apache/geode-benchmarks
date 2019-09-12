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
package org.apache.geode.benchmark.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

import org.apache.geode.benchmark.LongRange;

class PrePopulateRegionLongTest {

  @Test
  public void putsEntriesForServer() throws InterruptedException {
    final LongRange range = new LongRange(0, 100);
    PrePopulateRegionLong prePopulateRegionLong = new PrePopulateRegionLong(range);

    Map<Long, Long> region = new ConcurrentHashMap<>();

    prePopulateRegionLong.run(region, range.sliceFor(2, 1));

    // verify that we put the last 50 keys
    verifyKeys(region, 50, 100);
  }

  @Test
  public void putsEntriesForServerWithSmallBatches() throws InterruptedException {
    final LongRange range = new LongRange(0, 100);
    PrePopulateRegionLong prePopulateRegionLong = new PrePopulateRegionLong(range);
    prePopulateRegionLong.setBatchSize(2);

    Map<Long, Long> region = new ConcurrentHashMap<>();

    prePopulateRegionLong.run(region, range.sliceFor(2, 1));

    // verify that we put the last 50 keys
    verifyKeys(region, 50, 100);
  }

  private void verifyKeys(Map<Long, Long> region, int startInclusive, int endExclusive) {
    List<Long> expectedKeys = LongStream.range(startInclusive, endExclusive)
        .mapToObj(Long::new)
        .collect(Collectors.toList());

    assertThat(region.keySet()).containsExactlyInAnyOrderElementsOf(expectedKeys);
  }

}
