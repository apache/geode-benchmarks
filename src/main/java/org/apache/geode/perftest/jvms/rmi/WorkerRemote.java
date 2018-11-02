package org.apache.geode.perftest.jvms.rmi;

import java.rmi.Remote;

import org.apache.geode.perftest.Task;

public interface WorkerRemote extends Remote {
  void execute(Task task) throws Exception;
}
