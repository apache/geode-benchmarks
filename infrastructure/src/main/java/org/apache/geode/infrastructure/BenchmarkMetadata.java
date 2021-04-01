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
package org.apache.geode.infrastructure;

/**
 * Static methods to generate common strings used by the infrastructure.
 */
public class BenchmarkMetadata {
  public static String PREFIX = "geode-benchmarks";

  public static String benchmarkPrefix(String tag) {
    if (System.getProperty("TEST_CI").equals("1")) {
      return PREFIX + "-" + tag;
    } else {
      return PREFIX + "-" + System.getProperty("user.name") + "-" + tag;
    }
  }

  public static String benchmarkString(String tag, String suffix) {
    return benchmarkPrefix(tag) + "-" + suffix;
  }

  public static String benchmarkConfigDirectory() {
    return System.getProperty("user.home") + "/." + PREFIX;
  }


  public static String benchmarkPrivateKeyFileName(String tag) {
    return benchmarkConfigDirectory() + "/" + tag + "-privkey.pem";
  }

  public static String benchmarkMetadataFileName(String tag) {
    return benchmarkConfigDirectory() + "/" + tag + "-cluster-launch.properties";
  }
}
