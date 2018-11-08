package org.apache.geode.perftest.infrastructure.jclouds;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.apache.commons.io.FileUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.sshj.config.SshjSshClientModule;

import org.apache.geode.perftest.infrastructure.CommandResult;
import org.apache.geode.perftest.infrastructure.Infrastructure;

public class GoogleCloudInfrastructure implements Infrastructure {

  public static final String CREDENTIALS_ENV = "GOOGLE_APPLICATION_CREDENTIALS";
  private final Set<GoogleCloudNode> nodes;
  private final ComputeService client;

  public GoogleCloudInfrastructure(int nodeCount) throws IOException {

    String credentialsFile = System.getenv(CREDENTIALS_ENV);
    if(credentialsFile == null) {
      throw new IllegalStateException("Must set the environment variable " + CREDENTIALS_ENV + " to a valid google cloud json credentials file.");
    }

    String fileContents = FileUtils.readFileToString(new File(credentialsFile), Charset.defaultCharset());

    Supplier<Credentials> credentials = new GoogleCredentialsFromJson(fileContents);
    ComputeServiceContext context = ContextBuilder.newBuilder("google-compute-engine")
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

    nodes = new HashSet<>();
    Iterator<? extends NodeMetadata> nodeItr = nodeMetadata.iterator();
    for(int i = 0; i < nodeCount; i++) {
      NodeMetadata metadata = nodeItr.next();
      client.
      nodes.add(new GoogleCloudNode(metadata));
    }
  }

  @Override
  public Set<Node> getNodes() {
    return Collections.unmodifiableSet(nodes);
  }

  @Override
  public CommandResult onNode(Node node, String[] shellCommand) throws IOException {
    NodeMetadata metadata = ((GoogleCloudNode) node).metadata;

    String script = String.join(" ", shellCommand);
    ExecResponse
        result =
        client.runScriptOnNode(metadata.getId(), script);

    return new CommandResult(result.getOutput(), result.getExitStatus());
  }

  @Override
  public void delete() throws InterruptedException, IOException {

  }

  @Override
  public void copyToNodes(Iterable<File> files, String destDir) throws IOException {

  }

  @Override
  public void copyFromNode(Node node, String directory, File destDir) throws IOException {

  }

  private class GoogleCloudNode implements Node {
    private NodeMetadata metadata;

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
