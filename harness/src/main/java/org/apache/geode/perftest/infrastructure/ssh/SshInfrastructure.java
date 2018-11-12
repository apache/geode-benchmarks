package org.apache.geode.perftest.infrastructure.ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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

  public SshInfrastructure(Collection<String> hosts, String user) {
    this.hosts = hosts.stream().map(SshNode::new).collect(Collectors.toSet());
    this.user = user;
  }

  SSHClient getSSHClient(Node node) throws IOException {
    SSHClient client = new SSHClient();
    client.addHostKeyVerifier(new PromiscuousVerifier());
    client.loadKnownHosts();
    client.connect(node.getAddress());
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
    try (SSHClient client = getSSHClient(node)) {

      String script = "'" + String.join("' '", shellCommand) + "'";

      try (Session session = client.startSession()) {
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
  public void copyToNodes(Iterable<File> files, String destDir) throws IOException {
    for(Node node : getNodes()) {
      try (SSHClient client = getSSHClient(node)) {
        try (Session session = client.startSession()) {
          client.useCompression();

          String script = "mkdir -p " + destDir;
          final Session.Command cmd = session.exec(script);
          cmd.join();
          for (File file : files) {
            logger.info("Copying " + file + " to " + node.getAddress());
            client.newSCPFileTransfer().upload(new FileSystemFile(file), destDir);
          }
          return;
        }
      }
    }
  }

  @Override
  public void copyFromNode(Node node, String directory, File destDir) throws IOException {
    try (SSHClient client = getSSHClient(node)) {

      try (Session session = client.startSession()) {
        client.useCompression();

        destDir.mkdirs();
        client.newSCPFileTransfer().download(directory, destDir.getPath());
        return;
      }
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
