package org.apache.geode.perftest.infrastructure.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.geode.perftest.infrastructure.Infrastructure;

public class LocalInfrastructure implements Infrastructure {

  private static final Node LOCAL_NODE = new Node() { };

  private final List<Process> processList = new ArrayList<Process>();


  @Override
  public Set<Node> getNodes() {
    return Collections.singleton(LOCAL_NODE);
  }

  @Override
  public void onNode(Node node, String[] shellCommand) throws IOException {
    //Ignore the node parameter, everything is created locally

    Path workingDir = Files.createTempDirectory("workerProcess");

    ProcessBuilder builder = new ProcessBuilder();
    builder.command(shellCommand);
    builder.inheritIO();
    builder.directory(workingDir.toFile());

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
}
