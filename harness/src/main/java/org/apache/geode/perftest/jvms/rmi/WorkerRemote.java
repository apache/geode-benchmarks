package org.apache.geode.perftest.jvms.rmi;

import java.rmi.Remote;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public interface WorkerRemote extends Remote {
  void execute(Task task, TestContext context) throws Exception;
}
