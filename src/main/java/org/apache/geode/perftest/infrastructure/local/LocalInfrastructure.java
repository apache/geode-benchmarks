package org.apache.geode.perftest.infrastructure.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.geode.perftest.infrastructure.Infrastructure;

public class LocalInfrastructure implements Infrastructure {

  private final Set<LocalNode> nodes = new HashSet<>();
  private final List<Process> processList = new ArrayList<Process>();

  public LocalInfrastructure(int numNodes) throws IOException {
    for(int i =0; i < numNodes; i++) {
      Path workingDir = Files.createTempDirectory("workerProcess");
      nodes.add(new LocalNode(workingDir.toFile()));
    }
  }

  @Override
  public Set<Node> getNodes() {
    return Collections.unmodifiableSet(nodes);
  }

  @Override
  public void onNode(Node node, String[] shellCommand) throws IOException {
    //Ignore the node parameter, everything is created locally


    ProcessBuilder builder = new ProcessBuilder();
    builder.command(shellCommand);
    builder.inheritIO();
    builder.directory(((LocalNode)node).getWorkingDir());

    Process process = builder.start();
    processList.add(process);
  }

  @Override
  public void delete() throws InterruptedException {
    for(Process process : processList) {
      process.destroyForcibly();
      process.waitFor();
      //TODO - delete directory
    }
  }

  @Override
  public void copyFiles(Iterable<File> files, String destDirName) throws IOException {
    for(LocalNode node : nodes) {
      Path destDir = new File(node.getWorkingDir(), destDirName).toPath();
      destDir.toFile().mkdir();
      for(File file : files) {
        Files.copy(file.toPath(), destDir.resolve(file.getName()));
      }
    }
  }

  private static class LocalNode implements Node {
    File workingDir;

    public LocalNode(File workingDir) {
      this.workingDir = workingDir;
    }

    public File getWorkingDir() {
      return workingDir;
    }
  }
}
