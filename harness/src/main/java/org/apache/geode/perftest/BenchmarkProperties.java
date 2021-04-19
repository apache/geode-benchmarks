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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkProperties {

  /**
   * Read the current system properties and find any system properties that start
   * with "benchmark.system.ROLE" Returns a map of ROLE-> system properties for that
   * role.
   */
  public static Map<String, List<String>> getDefaultJVMArgs() {
    Map<String, List<String>> results = new HashMap<>();
    System.getProperties().stringPropertyNames().stream()
        .filter(name -> name.startsWith("benchmark.system."))
        .forEach(name -> {
          String shortName = name.replace("benchmark.system.", "");
          String[] roleAndProperty = shortName.split("\\.", 2);
          String role = roleAndProperty[0];
          String property = roleAndProperty[1];
          String value = System.getProperty(name);
          List<String> roleProperties = results.computeIfAbsent(role, key -> new ArrayList<>());
          roleProperties.add("-D" + property + "=" + value);
        });
    return results;
  }
}
