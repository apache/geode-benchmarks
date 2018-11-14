package org.apache.geode.perftest.jvms;

import java.rmi.NoSuchObjectException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.geode.perftest.Task;

import org.apache.geode.perftest.TestContext;
import org.apache.geode.perftest.jvms.rmi.Controller;

/**
 * Interface for accessing remote JVMs are running tasks on them.
 */
public class RemoteJVMs implements AutoCloseable {
  private final List<JVMManager.JVMMapping> jvmMappings;
  private final Controller controller;
  private final TestContext context;
  private final Registry registry;
  private final CompletableFuture<Void> exited;


  public RemoteJVMs(List<JVMManager.JVMMapping> mapping, Controller controller, Registry registry,
                    CompletableFuture<Void> exited) {
    this.jvmMappings = mapping;
    this.controller = controller;
    this.context = new TestContext(jvmMappings);
    this.registry = registry;
    this.exited = exited;
  }


  /**
   * Run a task in parallel on all JVMs with the given roles.
   */
  public void execute(Task task, String ... roleArray) {

    HashSet<String> roles = new HashSet<>(Arrays.asList(roleArray));

    Stream<CompletableFuture> futures = jvmMappings.stream()
        .filter(mapping -> roles.contains(mapping.getRole()))
        .map(mapping -> controller.onWorker(mapping.getId(), task, context));

    futures.forEach(CompletableFuture::join);
  }

  public void close() throws NoSuchObjectException, ExecutionException, InterruptedException {
    controller.close();
    exited.get();
    UnicastRemoteObject.unexportObject(controller,true);
    UnicastRemoteObject.unexportObject(registry, true);
  }
}
