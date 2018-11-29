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

package org.apache.geode.perftest.infrastructure.local;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import org.apache.geode.perftest.infrastructure.Infrastructure;

/**
 * Implementation of infrastructure that just runs commands
 * on the local computer, in their own working directories.
 */
public class LocalInfrastructure implements Infrastructure {

  private final Set<LocalNode> nodes = new LinkedHashSet<>();
  private final List<Process> processList = new ArrayList<Process>();

  public LocalInfrastructure(int numNodes) throws IOException {
    for (int i = 0; i < numNodes; i++) {
      Path workingDir = Files.createTempDirectory("workerProcess");
      nodes.add(new LocalNode(workingDir.toFile()));
    }
  }

  @Override
  public Set<Node> getNodes() {
    return Collections.unmodifiableSet(nodes);
  }

  @Override
  public int onNode(Node node, String[] shellCommand)
      throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder();
    builder.command(shellCommand);
    builder.inheritIO();
    builder.directory(((LocalNode) node).getWorkingDir());

    System.out.println(String.format("Lauching %s>%s", ((LocalNode) node).getWorkingDir(),
        String.join(" ", shellCommand)));
    Process process = builder.start();
    processList.add(process);

    int exitCode = process.waitFor();
    return exitCode;
  }

  @Override
  public void close() throws InterruptedException, IOException {
    for (Process process : processList) {
      process.destroyForcibly();
      process.waitFor();
    }

    for (LocalNode node : nodes) {
      FileUtils.deleteDirectory(node.getWorkingDir());
    }
  }

  @Override
  public void copyToNodes(Iterable<File> files, String destDirName, boolean removeExisting)
      throws IOException {
    for (LocalNode node : nodes) {
      Path destDir = new File(node.getWorkingDir(), destDirName).toPath();
      destDir.toFile().mkdirs();

      for (File file : files) {
        Files.copy(file.toPath(), destDir.resolve(file.getName()));
      }
    }
  }

  @Override
  public void copyFromNode(Node node, String directory, File destDir) throws IOException {
    File nodeDir = new File(((LocalNode) node).workingDir, directory);

    if (!nodeDir.exists()) {
      return;
    }

    FileUtils.copyDirectory(nodeDir, destDir);
  }

  static class LocalNode implements Node {
    File workingDir;

    public LocalNode(File workingDir) {
      this.workingDir = workingDir;
    }

    public File getWorkingDir() {
      return workingDir;
    }

    @Override
    public InetAddress getAddress() {
      try {
        return InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
        throw new UncheckedIOException(e);
      }
    }
  }
}
