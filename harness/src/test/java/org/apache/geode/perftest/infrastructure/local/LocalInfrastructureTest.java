package org.apache.geode.perftest.infrastructure.local;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LocalInfrastructureTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private LocalInfrastructure infra;

  @Before
  public void createInfra() throws IOException {
    infra = new LocalInfrastructure(1);
  }

  @After
  public void deleteInfra() throws IOException, InterruptedException {
    infra.delete();
  }


  @Test
  public void copyToFilePutsFileOnNode() throws IOException, InterruptedException {
    LocalInfrastructure.LocalNode node =
        (LocalInfrastructure.LocalNode) infra.getNodes().iterator().next();

    File nodedir = node.workingDir;

    File someFile = temporaryFolder.newFile();

    File expectedDir = new File(nodedir, "lib");
    assertFalse(expectedDir.exists());
    infra.copyFiles(Arrays.asList(someFile), "lib");
    assertTrue(expectedDir.exists());
    assertTrue(new File(expectedDir, someFile.getName()).exists());


    infra.delete();

    assertFalse(expectedDir.exists());
  }

  @Test
  public void onNodeExecutesShellCommand() throws IOException, InterruptedException {
    LocalInfrastructure.LocalNode node =
        (LocalInfrastructure.LocalNode) infra.getNodes().iterator().next();

    File nodedir = node.workingDir;

    File expectedFile = new File(nodedir, "tmpFile");
    assertFalse(expectedFile.exists());

    infra.onNode(node, new String[] {"touch", "tmpFile"});

    //On node is asynchronous
    Awaitility.await().until(() -> expectedFile.exists());
  }

}