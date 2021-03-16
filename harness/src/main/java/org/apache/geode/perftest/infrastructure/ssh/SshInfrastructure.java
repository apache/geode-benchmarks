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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Signal;
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
  public static final Config CONFIG = new QuietSshLoggingConfig();
  private ExecutorService streamReaderThreadPool = Executors.newCachedThreadPool();

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
  public int onNode(final Node node, final String[] shellCommand) throws IOException {
    try (final SSHClient client = getSSHClient(node.getAddress());
        final Session session = client.startSession()) {
      final String script = "'" + String.join("' '", shellCommand) + "'";
      logger.debug("Executing {} on {}", script, node.getAddress());
      try (final Session.Command cmd = session.exec(script)) {
        final CompletableFuture<Void> copyStdout =
            copyStreamAsynchronously(cmd.getInputStream(), System.out);
        final CompletableFuture<Void> copyStdErr =
            copyStreamAsynchronously(cmd.getErrorStream(), System.err);

        cmd.join();
        copyStdout.join();
        copyStdErr.join();

        final Boolean coreDumped = cmd.getExitWasCoreDumped();
        if (null != coreDumped && coreDumped) {
          logger.error("Core dumped for {} on {}", script, node.getAddress());
        }
        final String errorMessage = cmd.getExitErrorMessage();
        if (null != errorMessage) {
          logger.error("Exit error message for {} on {} was \"{}\"", script, node.getAddress(),
              errorMessage);
        }
        final Signal exitSignal = cmd.getExitSignal();
        if (null != exitSignal) {
          logger.error("Exit signal for {} on {} was {}", script, node.getAddress(), exitSignal);
        }

        final Integer exitStatus = cmd.getExitStatus();
        if (null == exitStatus) {
          logger.error("Exit status for {} on {} was null", script, node.getAddress());
          return -1;
        }
        return exitStatus;
      }
    }
  }

  private CompletableFuture<Void> copyStreamAsynchronously(InputStream inputStream,
      PrintStream out) {
    return CompletableFuture.runAsync(() -> {
      try {
        org.apache.commons.io.IOUtils.copy(inputStream, out);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }, streamReaderThreadPool);
  }

  private void copyStream(InputStream inputStream, PrintStream out) {
    try {
      org.apache.commons.io.IOUtils.copy(inputStream, out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() throws InterruptedException, IOException {

  }

  @Override
  public void copyToNodes(Iterable<File> files, Function<Node, String> destDirFunction,
      boolean removeExisting)
      throws IOException {

    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (Node node : this.getNodes()) {
      futures.add(CompletableFuture.runAsync(() -> {
        InetAddress address = node.getAddress();
        String destDir = destDirFunction.apply(node);
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
            logger.debug("Copying " + file + " to " + address);
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
