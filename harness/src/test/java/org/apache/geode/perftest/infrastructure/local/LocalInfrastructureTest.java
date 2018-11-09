package org.apache.geode.perftest.infrastructure.local;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.perftest.infrastructure.CommandResult;

public class LocalInfrastructureTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private LocalInfrastructure infra;
  private LocalInfrastructure.LocalNode node;

  @Before
  public void createInfra() throws IOException {
    infra = new LocalInfrastructure(1);
    node = (LocalInfrastructure.LocalNode) infra.getNodes().iterator().next();
  }

  @After
  public void deleteInfra() throws IOException, InterruptedException {
    infra.delete();
  }


  @Test
  public void copyToNodesPutsFileOnNode() throws IOException, InterruptedException {

    File nodedir = node.workingDir;

    File someFile = temporaryFolder.newFile();

    File expectedDir = new File(nodedir, "lib");
    assertFalse(expectedDir.exists());
    infra.copyToNodes(Arrays.asList(someFile), "lib");
    assertTrue(expectedDir.exists());
    assertTrue(new File(expectedDir, someFile.getName()).exists());


    infra.delete();

    assertFalse(expectedDir.exists());
  }

  @Test
  public void onNodeExecutesShellCommand()
      throws IOException, InterruptedException, ExecutionException {
    File nodedir = node.workingDir;

    File expectedFile = new File(nodedir, "tmpFile");
    assertFalse(expectedFile.exists());

    CompletableFuture<CommandResult> future = infra.onNode(node, new String[] {"touch", "tmpFile"});

    CommandResult result = future.get();

    assertEquals(0, result.getExitStatus());

    expectedFile.exists();
  }

  @Test
  public void copyFromNodeCopiesFileFromNode() throws IOException {

    File newFile = new File(node.workingDir, "someFile");
    newFile.createNewFile();

    File destDirectory = temporaryFolder.newFolder();
    infra.copyFromNode(node, ".", destDirectory);

    assertTrue(new File(destDirectory, "someFile").exists());

  }

}