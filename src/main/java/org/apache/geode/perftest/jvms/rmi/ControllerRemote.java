package org.apache.geode.perftest.jvms.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.geode.perftest.jvms.rmi.WorkerRemote;

public interface ControllerRemote extends Remote {

  void addWorker(int id, WorkerRemote remote) throws RemoteException;

  boolean ping() throws RemoteException;
}
