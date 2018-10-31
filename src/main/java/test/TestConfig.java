package test;

import java.io.Serializable;

public interface TestConfig extends Serializable {

  static TestConfig create() {
    return null;
  }

  void roles(String ... roles);

  void before(Task task, String ... roles);

  void workload(Task task, String ... roles);

  void after(Task task, String ... roles);
}
