package org.apache.geode.perftest.infrastructure.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.perftest.infrastructure.CommandResult;
import org.apache.geode.perftest.infrastructure.Infrastructure;

public class SshInfrastructureTest {
  private static final Set<String> HOSTS = Collections.singleton("localhost");
  private static final String USER = System.getProperty("user.name");
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void canFindNodes() throws IOException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER);

    assertEquals(1, infra.getNodes().size());
  }

  @Test
  public void canExecuteACommandOnNode()
      throws IOException, ExecutionException, InterruptedException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER);
    Infrastructure.Node node1 = infra.getNodes().iterator().next();

    CommandResult result = infra.onNode(node1, new String[] {"echo", "hello"});

    assertEquals(0, result.getExitStatus());
    assertTrue(result.getOutput().contains("hello"));
  }

  @Test
  public void copyToNodesPutsFileOnNode() throws IOException, InterruptedException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER);

    File someFile = temporaryFolder.newFile();
    File targetFolder = new File(temporaryFolder.newFolder(), "dest");

    assertFalse(targetFolder.exists());

    infra.copyToNodes(Arrays.asList(someFile), targetFolder.getPath());

    assertTrue(targetFolder.exists());
    assertTrue(new File(targetFolder, someFile.getName()).exists());
  }


  @Test
  public void canCopyFilesFromANode()
      throws IOException, ExecutionException, InterruptedException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER);
    Infrastructure.Node node1 = infra.getNodes().iterator().next();

    infra.onNode(node1, new String[] {"mkdir", "-p", "/tmp/foo"});
    infra.onNode(node1, new String[] {"touch", "/tmp/foo/file.txt"});
    infra.onNode(node1, new String[] {"touch", "/tmp/foo/file2.txt"});

    File destDirectory = temporaryFolder.newFolder();

    infra.copyFromNode(node1,"/tmp/foo", destDirectory);
    assertTrue(new File(destDirectory,"foo/file.txt").exists());
    assertTrue(new File(destDirectory,"foo/file2.txt").exists());

  }

}