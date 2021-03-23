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

public enum JavaVersion {
  v8, v11, v12, v13, v16;

  public static JavaVersion current() {
    return valueOfVersion(System.getProperty("java.runtime.version"));
  }

  private static JavaVersion valueOfVersion(final String javaVersion) {
    if (javaVersion.startsWith("1.8")) {
      return v8;
    } else if (javaVersion.startsWith("11.")) {
      return v11;
    } else if (javaVersion.startsWith("12.")) {
      return v12;
    } else if (javaVersion.matches("^13\\b.*")) {
      return v13;
    } else if (javaVersion.matches("^16\\b.*")) {
      return v16;
    }
    throw new IllegalStateException("Unknown version " + javaVersion);
  }

  public boolean atLeast(final JavaVersion other) {
    return this.compareTo(other) >= 0;
  }

  public boolean olderThan(final JavaVersion other) {
    return this.compareTo(other) < 0;
  }
}
