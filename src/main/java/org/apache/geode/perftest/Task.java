package org.apache.geode.perftest;

import java.io.Serializable;

/**
 * A single task in a test, such as initializing a member
 * or doing a single operation during the workload phase.
 */
public interface Task extends Serializable {

  /**
   * Execute the task on the remote machine
   */
  void run(TestContext context) throws Exception;
}
