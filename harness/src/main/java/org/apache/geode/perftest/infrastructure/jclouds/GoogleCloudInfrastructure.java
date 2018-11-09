package org.apache.geode.perftest.infrastructure.jclouds;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Module;
import org.apache.commons.io.FileUtils;
import org.apache.geode.perftest.infrastructure.CommandResult;
import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.FilePayload;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class GoogleCloudInfrastructure implements Infrastructure {

  public static final String CREDENTIALS_ENV = "GOOGLE_APPLICATION_CREDENTIALS";
  private final Set<GoogleCloudNode> nodes;
  private final ComputeService client;
  private final ComputeServiceContext context;

  public GoogleCloudInfrastructure(int nodeCount) throws IOException {

    Supplier<Credentials> credentials = getGoogleCredentials();

    context = ContextBuilder.newBuilder("google-compute-engine")
        .modules(ImmutableSet.<Module> of(new SshjSshClientModule()))
        .credentialsSupplier(credentials)
        .buildView(ComputeServiceContext.class);

    client = context.getComputeService();

    Set<? extends NodeMetadata>
        nodeMetadata =
        client.listNodesDetailsMatching(node -> node.getName().contains("yardstick-tester"));

    if(nodeMetadata.size() < nodeCount) {
      throw new IllegalStateException("Need at least " + nodeCount + " nodes. Have " + nodeMetadata.size());
    }

    final String key = getSshPrivateKey();
    LoginCredentials sshCredentials = LoginCredentials.builder().user("geode").privateKey(key).build();

    nodes = new HashSet<>();
    Iterator<? extends NodeMetadata> nodeItr = nodeMetadata.iterator();
    for(int i = 0; i < nodeCount; i++) {
      NodeMetadata metadata = nodeItr.next();
      nodes.add(new GoogleCloudNode(addCredentials(sshCredentials, metadata)));
    }
  }

  private NodeMetadata addCredentials(LoginCredentials sshCredentials, NodeMetadata metadata) {
    return NodeMetadataBuilder.fromNodeMetadata(metadata).credentials(sshCredentials).build();
  }

  private String getSshPrivateKey() throws IOException {
    File keyFile = new File(System.getProperty("user.home"), ".ssh/google_compute_engine");
    return FileUtils.readFileToString(keyFile, Charset .defaultCharset());
  }

  private Supplier<Credentials> getGoogleCredentials() throws IOException {
    String credentialsFile = System.getenv(CREDENTIALS_ENV);
    if(credentialsFile == null) {
      throw new IllegalStateException("Must set the environment variable "
          + CREDENTIALS_ENV
          + " to a valid google cloud json credentials file.");
    }
    String fileContents = FileUtils.readFileToString(new File(credentialsFile), Charset.defaultCharset());

    return new GoogleCredentialsFromJson(fileContents);
  }

  @Override
  public Set<Node> getNodes() {
    return Collections.unmodifiableSet(nodes);
  }

  @Override
  public CompletableFuture<CommandResult> onNode(Node node, String[] shellCommand) throws IOException {
    NodeMetadata metadata = ((GoogleCloudNode) node).metadata;

    String script = "'" + String.join("' '", shellCommand) + "'";

    String key = getSshPrivateKey();
    RunScriptOptions
        runScriptOptions =
        new RunScriptOptions().overrideLoginPrivateKey(key).overrideLoginUser("geode");

    System.out.println("Running " + script);
    ListenableFuture<ExecResponse>
        result = client.submitScriptOnNode(metadata.getId(), script, runScriptOptions);

    CompletableFuture<CommandResult> completableFuture = FutureUtil.toCompletableFuture(result)
       .thenApply(execResponse -> new CommandResult(execResponse.getOutput() +
           '\n' + execResponse.getError(), execResponse.getExitStatus()));

    return completableFuture;
  }

  @Override
  public void delete() throws InterruptedException, IOException {

  }

  @Override
  public void copyToNodes(Iterable<File> files, String destDir) throws IOException {
    for(GoogleCloudNode node : nodes) {
      SshClient ssh = context.utils().sshForNode().apply(node.getMetadata());
      ssh.connect();
      ssh.exec("mkdir " + destDir);
      for(File file : files) {
        ssh.put(destDir + "/" + file.getName(), new FilePayload(file));
      }
    }

  }

  @Override
  public void copyFromNode(Node node, String directory, File destDir) throws IOException {
    SshClient ssh = context.utils().sshForNode().apply(((GoogleCloudNode)node).getMetadata());
    ssh.connect();
    destDir.mkdirs();
    Path remoteDirectory = new File(directory).toPath();
    Path remoteDirectoryName = remoteDirectory.getFileName();
    Path remoteParent = remoteDirectory.getParent();
    String remoteArchiveFilename = remoteDirectoryName.toString() + ".tar";
    String remoteArchivePathname = remoteParent.toString() + "/" + remoteArchiveFilename;
    String[] shellCommand = new String[] {
      "tar", "zcvf", remoteArchivePathname, "-C", remoteParent.toString(), remoteDirectoryName.toString()
      };
    this.onNode(node,shellCommand);
    Payload tarPayload = ssh.get(remoteArchivePathname);


      InputStream tarStream = tarPayload.openStream();
      File targetFile = new File(destDir.toString()+ "/" +remoteArchiveFilename.toString());

      Files.copy(
              tarStream,
              targetFile.toPath(),
              StandardCopyOption.REPLACE_EXISTING);

  }

  private class GoogleCloudNode implements Node {
    private NodeMetadata metadata;

    public NodeMetadata getMetadata() {
      return metadata;
    }

    public GoogleCloudNode(NodeMetadata next) {
      this.metadata = next;
    }

    @Override
    public InetAddress getAddress() {
      String firstAddress =  metadata.getPublicAddresses().iterator().next();
      try {
        return InetAddress.getByName(firstAddress);
      } catch (UnknownHostException e) {
        throw new UncheckedIOException(e);
      }
    }
  }
}
