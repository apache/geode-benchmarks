package org.apache.geode.perftest.jvms.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class Worker extends UnicastRemoteObject implements WorkerRemote {

  private TestContext context;

  public Worker(TestContext context) throws RemoteException {
    super();
    this.context = context;
  }
  @Override
  public void execute(Task task) throws Exception {
    task.run(context);


  }
}
