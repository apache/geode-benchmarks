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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.infrastructure.Infrastructure;

public class SshInfrastructure implements Infrastructure {
  private static final Logger logger = LoggerFactory.getLogger(SshInfrastructure.class);

  private final Set<SshNode> hosts;
  private final String user;
  private final int port;
  public static final Config CONFIG = new DefaultConfig();

  public SshInfrastructure(Collection<String> hosts, String user) {
    this(hosts, user, 22);
  }

  public SshInfrastructure(Collection<String> hosts, String user, int port) {
    this.hosts = hosts.stream()
        .map(SshNode::new)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    this.user = user;
    this.port = port;
  }

  SSHClient getSSHClient(InetAddress address) throws IOException {
    SSHClient client = new SSHClient(CONFIG);
    client.addHostKeyVerifier(new PromiscuousVerifier());
    client.connect(address, port);
    client.authPublickey(user);
    return client;
  }

  @Override
  public Set<Node> getNodes() {
    return Collections.unmodifiableSet(hosts);
  }

  @Override
  public int onNode(Node node, String[] shellCommand)
      throws IOException {
    try (SSHClient client = getSSHClient(node.getAddress())) {

      String script = "'" + String.join("' '", shellCommand) + "'";

      try (Session session = client.startSession()) {
        logger.info("Executing " + script + " on " + node.getAddress());
        final Session.Command cmd = session.exec(script);
        cmd.join();
        copyStream(cmd.getInputStream(), System.out);
        copyStream(cmd.getErrorStream(), System.err);

        cmd.join();
        return cmd.getExitStatus();
      }
    }
  }

  private void copyStream(InputStream inputStream, PrintStream out) throws IOException {
    org.apache.commons.io.IOUtils.copy(inputStream, out);
  }

  @Override
  public void close() throws InterruptedException, IOException {

  }

  @Override
  public void copyToNodes(Iterable<File> files, String destDir, boolean removeExisting)
      throws IOException {
    Set<InetAddress> uniqueNodes =
        getNodes().stream().map(Node::getAddress).collect(Collectors.toSet());

    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (InetAddress address : uniqueNodes) {
      futures.add(CompletableFuture.runAsync(() -> {
        try (SSHClient client = getSSHClient(address)) {
          client.useCompression();

          if (removeExisting) {
            try (Session session = client.startSession()) {
              session.exec(String.format("rm -rf '%s'", destDir)).join();
            }
          }

          try (Session session = client.startSession()) {
            session.exec(String.format("mkdir -p '%s'", destDir)).join();
          }

          for (File file : files) {
            logger.info("Copying " + file + " to " + address);
            client.newSCPFileTransfer().upload(new FileSystemFile(file), destDir);
          }
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }));
    }
    futures.forEach(CompletableFuture::join);
  }

  @Override
  public void copyFromNode(Node node, String directory, File destDir) throws IOException {
    try (SSHClient client = getSSHClient(node.getAddress())) {
      client.useCompression();

      destDir.mkdirs();
      client.newSCPFileTransfer().download(directory, destDir.getPath());
      return;
    }

  }

  public static class SshNode implements Node {
    private final InetAddress host;

    public SshNode(String host) {
      try {
        this.host = InetAddress.getByName(host);
      } catch (UnknownHostException e) {
        throw new UncheckedIOException(e);
      }
    }

    @Override
    public InetAddress getAddress() {
      return host;
    }
  }
}
