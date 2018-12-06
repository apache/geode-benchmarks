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

package org.apache.geode.perftest;

import java.io.Serializable;

import org.yardstickframework.BenchmarkDriver;

/**
 * A declarative performance test. Users should implement
 * this interface and define their test using the passed in
 * {@link TestConfig} object.
 *
 * There are three phases to the test:
 * <ul>
 * <li>Before Tasks - executed sequentially before the test</li>
 * <li>Workload Tasks - executed in parallel repeatedly during the workload phase</li>
 * <li>After Tasks - executed sequentially after the test</li>
 * </ul>
 *
 * Each of these phases can be assigned to *roles*.
 *
 * The test should, at a minimum
 *
 * Define the roles by calling {@link TestConfig#role(String, int)}
 * Define one or more tasks by calling {@link TestConfig#before(Task, String...)},
 * {@link TestConfig#after(Task, String...)} or
 * {@link TestConfig#workload(BenchmarkDriver, String...)}
 */
public interface PerformanceTest extends Serializable {

  /**
   * Return the configuration for the test.
   */
  TestConfig configure();

}
