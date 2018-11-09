package org.apache.geode.perftest.infrastructure.jclouds;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.perftest.infrastructure.CommandResult;
import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.junit.rules.TemporaryFolder;

public class GoogleCloudInfrastructureTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void canFindNodes() throws IOException {
    GoogleCloudInfrastructure infra = new GoogleCloudInfrastructure(1);

    assertEquals(1, infra.getNodes().size());
  }

  @Test
  public void canExecuteACommandOnNode()
      throws IOException, ExecutionException, InterruptedException {
    GoogleCloudInfrastructure infra = new GoogleCloudInfrastructure(1);
    Infrastructure.Node node1 = infra.getNodes().iterator().next();

    CommandResult result = infra.onNode(node1, new String[] {"echo", "hello"});

    assertEquals(0, result.getExitStatus());
    assertTrue(result.getOutput().contains("hello"));
  }

  @Test
  public void canCopyFilesFromANode()
      throws IOException, ExecutionException, InterruptedException {
    GoogleCloudInfrastructure infra = new GoogleCloudInfrastructure(1);
    Infrastructure.Node node1 = infra.getNodes().iterator().next();

    infra.onNode(node1, new String[] {"mkdir", "-p", "/tmp/foo"});
    infra.onNode(node1, new String[] {"cp", "/var/log/syslog", "/tmp/foo/file.txt"});
    File destDirectory = temporaryFolder.newFolder();

    infra.copyFromNode(node1,"/tmp/foo", destDirectory);
    assertTrue(new File(destDirectory,"foo.tar").exists());

  }

}