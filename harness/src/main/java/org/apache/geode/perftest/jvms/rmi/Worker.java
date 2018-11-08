package org.apache.geode.perftest.jvms.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class Worker extends UnicastRemoteObject implements WorkerRemote {


  public Worker() throws RemoteException {
    super();
  }
  @Override
  public void execute(Task task, TestContext context) throws Exception {
    task.run(context);


  }
}
