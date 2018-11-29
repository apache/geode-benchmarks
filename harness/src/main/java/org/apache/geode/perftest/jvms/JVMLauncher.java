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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.jvms.rmi.ChildJVM;

class JVMLauncher {
  private static final Logger logger = LoggerFactory.getLogger(RemoteJVMFactory.class);

  JVMLauncher() {}

  CompletableFuture<Void> launchProcesses(Infrastructure infra, int rmiPort,
      List<JVMMapping> mapping, String libDir)
      throws UnknownHostException {
    List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
    for (JVMMapping entry : mapping) {
      CompletableFuture<Void> future = launchWorker(infra, rmiPort, libDir, entry);
      futures.add(future);
    }
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }

  CompletableFuture<Void> launchWorker(Infrastructure infra, int rmiPort, String libDir,
      JVMMapping jvmConfig)
      throws UnknownHostException {
    String[] shellCommand =
        buildCommand(InetAddress.getLocalHost().getHostAddress(), rmiPort, libDir, jvmConfig);

    CompletableFuture<Void> future = new CompletableFuture<Void>();
    Thread thread = new Thread("Worker " + jvmConfig.getNode().getAddress()) {
      public void run() {

        try {
          int result = infra.onNode(jvmConfig.getNode(), shellCommand);
          if (result != 0) {
            logger.error("ChildJVM exited with error code " + result);
          }
        } catch (Throwable t) {
          logger.error("Launching " + String.join(" ", shellCommand) + " on " + jvmConfig.getNode()
              + "Failed.", t);
        } finally {
          future.complete(null);
        }
      }
    };
    thread.start();

    return future;
  }

  String[] buildCommand(String rmiHost, int rmiPort, String libDir, JVMMapping jvmConfig) {

    List<String> command = new ArrayList<String>();
    command.add("java");
    command.add("-classpath");
    command.add(libDir + "/*");
    command.add("-D" + RemoteJVMFactory.RMI_HOST + "=" + rmiHost);
    command.add("-D" + RemoteJVMFactory.RMI_PORT_PROPERTY + "=" + rmiPort);
    command.add("-D" + RemoteJVMFactory.JVM_ID + "=" + jvmConfig.getId());
    command.add("-D" + RemoteJVMFactory.OUTPUT_DIR + "=" + jvmConfig.getOutputDir());
    command.addAll(jvmConfig.getJvmArgs());
    command.add(ChildJVM.class.getName());

    return command.toArray(new String[0]);
  }
}
