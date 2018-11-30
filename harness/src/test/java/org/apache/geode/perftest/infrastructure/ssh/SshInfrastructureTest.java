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

import org.apache.geode.perftest.infrastructure.Infrastructure;

public class SshInfrastructureTest {


  private static final Set<String> HOSTS = Collections.singleton("localhost");
  private static final String USER = System.getProperty("user.name");
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public SshServerRule server = new SshServerRule();

  @Test
  public void canFindNodes() throws IOException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER, server.getPort());

    assertEquals(1, infra.getNodes().size());
  }

  @Test
  public void canExecuteACommandOnNode()
      throws IOException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER, server.getPort());
    Infrastructure.Node node1 = infra.getNodes().iterator().next();

    File folder = temporaryFolder.newFolder();
    folder.mkdirs();
    File expectedFile = new File(folder, "somefile.txt").getAbsoluteFile();
    int result = infra.onNode(node1, new String[] {"touch", expectedFile.getPath()});

    assertEquals(0, result);
    assertTrue(expectedFile.exists());
  }

  @Test
  public void copyToNodesPutsFileOnNode() throws IOException, InterruptedException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER, server.getPort());

    File someFile = temporaryFolder.newFile();
    File targetFolder = new File(temporaryFolder.newFolder(), "dest");

    assertFalse(targetFolder.exists());

    infra.copyToNodes(Arrays.asList(someFile), targetFolder.getPath(), false);

    assertTrue(targetFolder.exists());
    assertTrue(new File(targetFolder, someFile.getName()).exists());
  }

  @Test
  public void copyToNodesCleansDirectory() throws IOException, InterruptedException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER, server.getPort());

    File someFile = temporaryFolder.newFile();
    File targetFolder = new File(temporaryFolder.newFolder(), "dest");

    targetFolder.mkdirs();
    File fileToRemove = new File(targetFolder, "removethis");
    fileToRemove.createNewFile();
    assertTrue(fileToRemove.exists());

    infra.copyToNodes(Arrays.asList(someFile), targetFolder.getPath(), true);

    assertTrue(targetFolder.exists());
    assertTrue(new File(targetFolder, someFile.getName()).exists());
    assertFalse(fileToRemove.exists());
  }


  @Test
  public void canCopyFilesFromANode()
      throws IOException, ExecutionException, InterruptedException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER, server.getPort());
    Infrastructure.Node node1 = infra.getNodes().iterator().next();

    infra.onNode(node1, new String[] {"mkdir", "-p", "/tmp/foo"});
    infra.onNode(node1, new String[] {"touch", "/tmp/foo/file.txt"});
    infra.onNode(node1, new String[] {"touch", "/tmp/foo/file2.txt"});

    File destDirectory = temporaryFolder.newFolder();

    infra.copyFromNode(node1, "/tmp/foo", destDirectory);
    assertTrue(new File(destDirectory, "foo/file.txt").exists());
    assertTrue(new File(destDirectory, "foo/file2.txt").exists());

  }

}
