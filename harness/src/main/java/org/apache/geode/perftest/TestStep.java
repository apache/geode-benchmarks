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
