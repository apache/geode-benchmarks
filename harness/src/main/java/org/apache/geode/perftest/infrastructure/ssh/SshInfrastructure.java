package org.apache.geode.perftest.infrastructure.ssh;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.geode.perftest.infrastructure.CommandResult;
import org.apache.geode.perftest.infrastructure.Infrastructure;

public class SshInfrastructure implements Infrastructure {
  public SshInfrastructure(Set<String> hosts) {

  }

  @Override
  public Set<Node> getNodes() {
    return null;
  }

  @Override
  public CompletableFuture<CommandResult> onNode(Node node, String[] shellCommand)
      throws IOException {
    return null;
  }

  @Override
  public void delete() throws InterruptedException, IOException {

  }

  @Override
  public void copyToNodes(Iterable<File> files, String destDir) throws IOException {

  }

  @Override
  public void copyFromNode(Node node, String directory, File destDir) throws IOException {

  }
}
