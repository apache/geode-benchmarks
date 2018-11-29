/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.perftest.jvms.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.runner.SharedContext;

/**
 * RMI object that lives on the main controller JVM
 */
public class Controller extends UnicastRemoteObject implements ControllerRemote {
  private final Registry registry;
  private final SharedContext context;
  private final Map<Integer, WorkerRemote> workers = new ConcurrentHashMap<>();
  private final CountDownLatch workersStarted;
  private volatile boolean isClosed;


  Controller(int numWorkers, Registry registry, SharedContext context) throws RemoteException {
    this.workersStarted = new CountDownLatch(numWorkers);
    this.registry = registry;
    this.context = context;
  }

  public void close() throws NoSuchObjectException {
    isClosed = true;
    UnicastRemoteObject.unexportObject(this, true);
    UnicastRemoteObject.unexportObject(registry, true);
  }

  @Override
  public void addWorker(int id, WorkerRemote worker) throws RemoteException {
    this.workers.put(id, worker);
    workersStarted.countDown();
  }

  public boolean waitForWorkers(long time, TimeUnit timeUnit) throws InterruptedException {
    return workersStarted.await(time, timeUnit);
  }

  @Override
  public boolean ping() throws RemoteException {
    return !isClosed;
  }

  @Override
  public SharedContext getsharedContext() throws RemoteException {
    return context;
  }

  public CompletableFuture<Void> onWorker(int id, Task task) {
    WorkerRemote worker = workers.get(id);
    if (worker == null) {
      throw new IllegalStateException("Worker number " + id + " is not set");
    }

    return CompletableFuture.runAsync(() -> {
      try {
        worker.execute(task);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
}
