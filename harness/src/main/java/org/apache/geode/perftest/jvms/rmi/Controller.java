package org.apache.geode.perftest.jvms.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * RMI object that lives on the main controller JVM
 */
public class Controller extends UnicastRemoteObject implements ControllerRemote {
  private Map<Integer, WorkerRemote> workers = new HashMap<>();
  private Consumer<WorkerRemote> callback;

  public Controller(Consumer<WorkerRemote> callback) throws RemoteException {
    this.callback = callback;
  }

  @Override
  public void addWorker(int id, WorkerRemote worker) {
    this.workers.put(id, worker);
    this.callback.accept(worker);
  }

  @Override
  public boolean ping() {
    return true;
  }

  public CompletableFuture<Void> onWorker(int id, Task task, TestContext context) {
    WorkerRemote worker = workers.get(id);
    if(worker == null) {
      throw new IllegalStateException("Worker number " + id + " is not set");
    }

    return CompletableFuture.runAsync(() -> {
      try {
        worker.execute(task, context);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
}
