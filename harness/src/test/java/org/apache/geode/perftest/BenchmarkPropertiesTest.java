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

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class BenchmarkPropertiesTest {

  @Test
  @SetSystemProperty(key = "benchmark.system.role1.p1", value = "v1")
  @SetSystemProperty(key = "benchmark.system.role1.p2", value = "v2")
  public void canParsePropertiesForRoles() {
    Map<String, List<String>> defaultArgs =
        BenchmarkProperties.getDefaultJVMArgs();

    Assertions.assertThat(defaultArgs).containsOnlyKeys("role1");

    Assertions.assertThat(defaultArgs.get("role1")).containsExactlyInAnyOrder("-Dp1=v1", "-Dp2=v2");
  }

}
