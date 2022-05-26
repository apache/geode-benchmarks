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

import static org.apache.geode.distributed.ConfigurationProperties.SSL_KEYSTORE;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_KEYSTORE_PASSWORD;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_TRUSTSTORE;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_TRUSTSTORE_PASSWORD;
import static org.apache.geode.perftest.jvms.JavaVersion.v17;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.jvms.rmi.ChildJVM;

class JVMLauncher {
  private static final Logger logger = LoggerFactory.getLogger(RemoteJVMFactory.class);
  public static final String WITH_STRACE = "benchmark.withStrace";

  JVMLauncher() {}

  CompletableFuture<Void> launchProcesses(Infrastructure infra, int rmiPort,
      List<JVMMapping> mapping)
      throws UnknownHostException {
    List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
    for (JVMMapping entry : mapping) {
      CompletableFuture<Void> future = launchWorker(infra, rmiPort, entry);
      futures.add(future);
    }
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }

  CompletableFuture<Void> launchWorker(Infrastructure infra, int rmiPort,
      JVMMapping jvmConfig)
      throws UnknownHostException {
    final String[] shellCommand = traceCommand(
        buildCommand(InetAddress.getLocalHost().getHostAddress(), rmiPort, jvmConfig), jvmConfig);

    CompletableFuture<Void> future = new CompletableFuture<>();
    Thread thread = new Thread("Worker " + jvmConfig.getNode().getAddress()) {
      public void run() {

        try {
          infra.onNode(jvmConfig.getNode(), new String[] {"rm", "-rf", jvmConfig.getOutputDir()});
          infra.onNode(jvmConfig.getNode(), new String[] {"mkdir", "-p", jvmConfig.getOutputDir()});
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

  String[] traceCommand(final String[] command, JVMMapping jvmConfig) {
    List<String> strace = new ArrayList<>();

    if (Boolean.getBoolean(WITH_STRACE)) {
      strace.add("strace");
      strace.add("-o");
      strace.add(jvmConfig.getOutputDir() + "/java.strace");
      strace.add("-ttt");
      strace.add("-T");
      strace.add("-f");
      strace.add("-ff");
    }

    strace.addAll(Arrays.asList(command));

    return strace.toArray(new String[0]);
  }

  String[] buildCommand(String rmiHost, int rmiPort, JVMMapping jvmConfig) {

    final JavaVersion javaVersion = JavaVersion.current();

    List<String> command = new ArrayList<>();
    command.add(System.getProperty("java.home") + "/bin/java");
    if (javaVersion.atLeast(v17)) {
      command.add("@" + jvmConfig.getLibDir() + "/java.args");
    }
    command.add("-classpath");
    command.add(jvmConfig.getLibDir() + "/*");
    command.add("-Djava.library.path=" + System.getProperty("user.home") + "/META-INF/native");
    command.add("-D" + RemoteJVMFactory.RMI_HOST + "=" + rmiHost);
    command.add("-D" + RemoteJVMFactory.RMI_PORT_PROPERTY + "=" + rmiPort);
    command.add("-D" + RemoteJVMFactory.JVM_ID + "=" + jvmConfig.getId());
    command.add("-D" + RemoteJVMFactory.ROLE + "=" + jvmConfig.getRole());
    command.add("-D" + RemoteJVMFactory.OUTPUT_DIR + "=" + jvmConfig.getOutputDir());

    if (jvmConfig.getJvmArgs().contains("-Dbenchmark.withSsl=true")) {
      command
          .add("-Dgemfire." + SSL_KEYSTORE + "=" + jvmConfig.getLibDir() + "/temp-self-signed.jks");
      command.add("-Dgemfire." + SSL_KEYSTORE_PASSWORD + "=123456");
      command.add(
          "-Dgemfire." + SSL_TRUSTSTORE + "=" + jvmConfig.getLibDir() + "/temp-self-signed.jks");
      command.add("-Dgemfire." + SSL_TRUSTSTORE_PASSWORD + "=123456");
    }
    command.addAll(replaceTokens(jvmConfig.getJvmArgs(), jvmConfig));
    command.add(ChildJVM.class.getName());

    return command.toArray(new String[0]);
  }

  private static final List<String> replaceTokens(List<String> args, JVMMapping jvmConfig) {
    List<String> replaced = new ArrayList<>(args.size());
    for (String arg : args) {
      replaced.add(replaceTokens(arg, jvmConfig));
    }
    return replaced;
  }

  private static String replaceTokens(String arg, JVMMapping jvmConfig) {
    arg = arg.replace("OUTPUT_DIR", jvmConfig.getOutputDir());
    arg = arg.replace("JVM_ROLE", jvmConfig.getRole());
    arg = arg.replace("JVM_ID", Integer.toString(jvmConfig.getId()));
    return arg;
  }
}
