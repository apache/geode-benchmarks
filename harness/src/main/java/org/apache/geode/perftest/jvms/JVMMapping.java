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

package org.apache.geode.perftest.jvms;

import java.io.Serializable;
import java.util.List;

import org.apache.geode.perftest.infrastructure.Infrastructure;

public class JVMMapping implements Serializable {
  private final Infrastructure.Node node;
  private final String role;
  private final int id;
  private final List<String> jvmArgs;

  public JVMMapping(Infrastructure.Node node, String role, int id,
      List<String> jvmArgs) {
    this.node = node;
    this.role = role;
    this.id = id;
    this.jvmArgs = jvmArgs;
  }

  public Infrastructure.Node getNode() {
    return node;
  }

  public String getRole() {
    return role;
  }

  public int getId() {
    return id;
  }

  public String getOutputDir() {
    return "output/" + role + "-" + id;
  }

  public List<String> getJvmArgs() {
    return jvmArgs;
  }
}
