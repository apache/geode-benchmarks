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
package org.apache.geode.benchmark.tests;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.perftest.TestRunners;

@ExtendWith(TempDirectory.class)
public class ReplicatedIndexedAggregateQuerySumDistinctBenchmarkTest {
  private File folder;

  @BeforeEach
  void createTemporaryFolder(@TempDirectory.TempDir Path tempFolder) {
    folder = tempFolder.toFile();
  }

  @Test
  public void benchmarkRunsSuccessfully() throws Exception {
    ReplicatedIndexedAggregateQuerySumDistinctBenchmark test =
        new ReplicatedIndexedAggregateQuerySumDistinctBenchmark();
    test.setKeyRange(new LongRange(0, 100));
    test.setQueryRange(10);
    TestRunners.minimalRunner(folder).runTest(test);
  }

}
