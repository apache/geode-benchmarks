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

package org.apache.geode.perftest.jvms;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.jvms.rmi.Controller;

/**
 * Interface for accessing remote JVMs are running tasks on them.
 */
public class RemoteJVMs implements AutoCloseable {
  private final List<JVMMapping> jvmMappings;
  private final Controller controller;
  private final CompletableFuture<Void> exited;
  private final Infrastructure infra;


  public RemoteJVMs(Infrastructure infra,
      List<JVMMapping> mapping, Controller controller,
      CompletableFuture<Void> exited) {
    this.infra = infra;
    this.jvmMappings = mapping;
    this.controller = controller;
    this.exited = exited;
  }

  /**
   * Run a task in parallel on all JVMs with the given roles.
   */
  public void execute(Task task, String... roleArray) {

    HashSet<String> roles = new HashSet<>(Arrays.asList(roleArray));

    Stream<CompletableFuture> futures = jvmMappings.stream()
        .filter(mapping -> roles.contains(mapping.getRole()))
        .map(mapping -> controller.onWorker(mapping.getId(), task));

    futures.forEach(CompletableFuture::join);
  }

  public void close() throws IOException, ExecutionException, InterruptedException {
    controller.close();
    exited.get();
    infra.close();
  }

  /**
   * Copy results to the provided output directory
   */
  public void copyResults(File benchmarkOutput) throws IOException {
    benchmarkOutput.mkdirs();
    for (JVMMapping jvm : jvmMappings) {
      infra.copyFromNode(jvm.getNode(), jvm.getOutputDir(),
          new File(benchmarkOutput, jvm.getRole() + "-" + jvm.getId()));
    }
  }
}
