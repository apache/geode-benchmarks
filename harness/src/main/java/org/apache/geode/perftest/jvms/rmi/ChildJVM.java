package org.apache.geode.perftest.jvms.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.geode.perftest.TestContext;
import org.apache.geode.perftest.jvms.JVMManager;

/**
 * Main method for a JVM running on a remote node
 */
public class ChildJVM {

  public static void main(String[] args)
      throws RemoteException, NotBoundException, MalformedURLException, InterruptedException {

    String RMI_HOST = System.getProperty(JVMManager.RMI_HOST);
    String RMI_PORT = System.getProperty(JVMManager.RMI_PORT);
    int id = Integer.getInteger(JVMManager.JVM_ID);

    ControllerRemote controller = (ControllerRemote) Naming
        .lookup("//" + RMI_HOST + ":" + RMI_PORT + "/" + JVMManager.CONTROLLER);


    Worker worker = new Worker();

    controller.addWorker(id, worker);

    //Wait until the controller shuts down
    while(controller.ping()) {
      Thread.sleep(1000);
    }

    System.exit(0);
  }
}
