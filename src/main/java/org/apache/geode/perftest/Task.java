package org.apache.geode.perftest;

import java.io.Serializable;

public interface Task extends Serializable {

  void run(TestContext context) throws Exception;
}
