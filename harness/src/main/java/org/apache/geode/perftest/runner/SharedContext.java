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

package org.apache.geode.perftest.runner;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.geode.perftest.jvms.JVMMapping;

/**
 * Context for a running test that is the same for all JVMs
 * running the test. This context is created at the beginning of the
 * test run and passed to all JVMs.
 */
public class SharedContext implements Serializable {

  private List<JVMMapping> jvmMappings;

  public SharedContext(List<JVMMapping> jvmMappings) {

    this.jvmMappings = jvmMappings;
  }

  public Set<InetAddress> getHostsForRole(String role) {
    return jvmMappings.stream()
        .filter(mapping -> mapping.getRole().equals(role))
        .map(mapping -> mapping.getNode().getAddress())
        .collect(Collectors.toSet());
  }
}
