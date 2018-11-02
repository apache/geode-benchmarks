package org.apache.geode.perftest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestConfig implements Serializable {

  private Map<String, Integer> roles = new HashMap<>();
  private List<TestStep> before = new ArrayList<>();
  private List<TestStep> workload = new ArrayList<>();
  private List<TestStep> after = new ArrayList<>();

  public void role(String role, int numberOfJVMs) {
    this.roles.put(role, numberOfJVMs);
  }

  public Map<String, Integer> getRoles() {
    return roles;
  }

  public List<TestStep> getBefore() {
    return before;
  }

  public List<TestStep> getWorkload() {
    return workload;
  }

  public List<TestStep> getAfter() {
    return after;
  }

  public void before(Task task, String ... roles) {
    before.add(new TestStep(task, roles));
  }

  public void workload(Task task, String ... roles) {
    workload.add(new TestStep(task, roles));
  }

  public void after(Task task, String ... roles) {
    after.add(new TestStep(task, roles));
  }

  public static class TestStep {
    private final Task task;
    private final String[] roles;

    public TestStep(Task task, String[] roles) {
      this.task = task;
      this.roles = roles;
    }

    public Task getTask() {
      return task;
    }

    public String[] getRoles() {
      return roles;
    }
  }
}
