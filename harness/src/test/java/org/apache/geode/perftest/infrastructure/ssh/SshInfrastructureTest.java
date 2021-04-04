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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.apache.geode.perftest.infrastructure.Infrastructure;

public class SshInfrastructureTest {

  private static final Set<String> HOSTS = Collections.singleton("localhost");
  private static final String USER = System.getProperty("user.name");

  @TempDir
  Path temporaryFolder;

  public SshServer server;

  @BeforeEach
  void createTempFolder(@TempDir Path serverPath) throws IOException {
    server = createServer(serverPath);
  }

  private SshServer createServer(Path serverPath) throws IOException {
    SshServer sshd = SshServer.setUpDefaultServer();
    sshd.setPort(0);
    sshd.setHost("localhost");
    sshd.setPublickeyAuthenticator((username, key, session) -> true);
    sshd.setKeyPairProvider(
        new SimpleGeneratorHostKeyProvider(serverPath.resolve("hostkey.ser")));
    sshd.setCommandFactory(new SshInfrastructureTest.UnescapingCommandFactory());
    sshd.start();
    return sshd;
  }

  @AfterEach
  void stopServer() {
    try {
      server.stop();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private class UnescapingCommandFactory extends ProcessShellCommandFactory {
    @Override
    public Command createCommand(final ChannelSession channel, final String command)
        throws IOException {
      return super.createCommand(channel, command.replace("'", ""));
    }
  }

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

    File folder = temporaryFolder.toFile();
    File expectedFile = new File(folder, "somefile.txt").getAbsoluteFile();
    int result = infra.onNode(node1, new String[] {"touch", expectedFile.getPath()});

    assertEquals(0, result);
    assertTrue(expectedFile.exists());
  }

  @Test
  public void copyToNodesPutsFileOnNode() throws IOException, InterruptedException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER, server.getPort());

    File someFile = temporaryFolder.resolve("someFile.tmp").toFile();
    assertTrue(someFile.createNewFile());
    File targetFolder = temporaryFolder.resolve("dest").toFile();

    assertFalse(targetFolder.exists());
    infra.copyToNodes(Collections.singletonList(someFile), node -> targetFolder.getPath(), false);

    assertTrue(targetFolder.exists());
    assertTrue(new File(targetFolder, someFile.getName()).exists());
  }

  @Test
  public void copyToNodesCleansDirectory() throws IOException, InterruptedException {
    SshInfrastructure infra = new SshInfrastructure(HOSTS, USER, server.getPort());

    File someFile = temporaryFolder.resolve("someFile.tmp").toFile();
    assertTrue(someFile.createNewFile());
    File targetFolder = temporaryFolder.resolve("dest").toFile();
    assertTrue(targetFolder.mkdirs());
    File fileToRemove = new File(targetFolder, "removethis");
    assertTrue(fileToRemove.createNewFile());
    assertTrue(fileToRemove.exists());

    infra.copyToNodes(Arrays.asList(someFile), node -> targetFolder.getPath(), true);

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

    File destDirectory = temporaryFolder.toFile();
    infra.copyFromNode(node1, "/tmp/foo", destDirectory);
    assertTrue(new File(destDirectory, "foo/file.txt").exists());
    assertTrue(new File(destDirectory, "foo/file2.txt").exists());

  }

}
