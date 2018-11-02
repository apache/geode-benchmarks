package org.apache.geode.perftest.jvms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.geode.perftest.Task;

import org.apache.geode.perftest.jvms.rmi.Controller;

public class RemoteJVMs {
  private final List<JVMManager.JVMMapping> jvmMappings;
  private final Controller controller;

  public RemoteJVMs(List<JVMManager.JVMMapping> jvmMappings, Controller controller) {

    this.jvmMappings = jvmMappings;
    this.controller = controller;
  }

  public void execute(Task task, String[] roleArray) {

    HashSet<String> roles = new HashSet<>(Arrays.asList(roleArray));

    Stream<CompletableFuture> futures = jvmMappings.stream()
        .filter(mapping -> roles.contains(mapping.getRole()))
        .map(mapping -> controller.onWorker(mapping.getId(), task));

    futures.forEach(CompletableFuture::join);
  }
}
