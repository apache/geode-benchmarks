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

import java.io.File;
import java.io.IOException;

public interface ProbeResultParser {
  // Given a output directory for a benchmark, parse out the data for the desired probe. Note that
  // this method may be passed several csv files for a run and is expected to appropriately
  // aggregate the result of interest.
  void parseResults(File benchmarkOutputDir) throws IOException;

  // Reset the parser to a clean state where parseResults can be called again
  void reset();

  // Get a single float value summarizing the data for the probe.
  double getProbeResult();

  // Get a text description of what the probe result is depicting
  String getResultDescription();
}
