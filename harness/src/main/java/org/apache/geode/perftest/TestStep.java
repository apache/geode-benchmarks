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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TestStep {
  private final Task task;
  private final Set<String> roles;

  public TestStep(Task task, String[] roles) {
    if (roles == null || roles.length == 0) {
      throw new IllegalStateException("Task " + task + " must be assigned to at least one role");
    }
    this.task = task;
    this.roles = new HashSet<>(Arrays.asList(roles));
  }

  public Task getTask() {
    return task;
  }

  public String[] getRoles() {
    return roles.toArray(new String[0]);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TestStep testStep = (TestStep) o;
    return task.equals(testStep.task) &&
        roles.equals(testStep.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(task, roles);
  }
}
