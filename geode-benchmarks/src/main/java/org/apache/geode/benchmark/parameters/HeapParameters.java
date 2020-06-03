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

package org.apache.geode.benchmark.parameters;

import static org.apache.geode.benchmark.parameters.Utils.configureAll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.TestConfig;

public class HeapParameters {
  private static final Logger logger = LoggerFactory.getLogger(HeapParameters.class);

  public static void configure(final TestConfig testConfig) {
    final String heap = System.getProperty("withHeap", "8g");
    logger.info("Configuring heap parameters {}.", heap);
    configureAll(testConfig, "-Xmx" + heap, "-Xms" + heap, "-Xss228k");
  }

}
