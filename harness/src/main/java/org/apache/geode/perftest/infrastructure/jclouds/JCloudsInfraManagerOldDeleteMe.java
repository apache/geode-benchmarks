package org.apache.geode.perftest.infrastructure.jclouds;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.domain.Credentials;
import org.jclouds.io.payloads.FilePayload;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;

import org.apache.geode.perftest.infrastructure.CommandResult;
import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.Infrastructure;

/**
 * {@link InfraManager} that uses jclouds to launch instances
 * on the given cloud provider.
 */
public class JCloudsInfraManagerOldDeleteMe implements InfraManager {


  private final String cloud;
  private final String image;
  private final Supplier<Credentials> credentials;
  private final String group;
  private final boolean reuseExisting;
  private final boolean deleteAfterComplete;


  public JCloudsInfraManagerOldDeleteMe(String cloud,
                                        String image,
                                        Supplier<Credentials> credentials,
                                        String group,
                                        boolean reuseExisting,
                                        boolean deleteAfterComplete) {
    this.cloud = cloud;
    this.image = image;
    this.credentials = credentials;
    this.group = group;
    this.reuseExisting = reuseExisting;
    this.deleteAfterComplete = deleteAfterComplete;
  }

  @Override
  public Infrastructure create(int numNodes) throws RunNodesException {
    ComputeServiceContext context = ContextBuilder.newBuilder(cloud)
        .modules(ImmutableSet.<Module> of(new SshjSshClientModule() ))
        .credentialsSupplier(credentials)
        .buildView(ComputeServiceContext.class);

    ComputeService client = context.getComputeService();


    Set<NodeMetadata> nodes = new HashSet<>();
    if(reuseExisting) {
       nodes.addAll(client.listNodesDetailsMatching(node -> node.getGroup().equals(group)));
    }

    int remaining = numNodes - nodes.size();

    if(remaining > 0) {
      createAdditionalNodes(client, nodes, remaining);
    }

    return new TestInfraStructure(context, client, nodes);
  }

  private void createAdditionalNodes(ComputeService client, Set<NodeMetadata> nodes, int remaining)
      throws RunNodesException {
    Template
        template =
        client.templateBuilder()
            .imageNameMatches(image)
            .locationId("us-central1-c")
            .minCores(8)
            .build();
    nodes.addAll(client.createNodesInGroup(group, remaining, template));
  }

  private class TestInfraStructure implements Infrastructure {
    private final Set<JCloudsNode> nodes;
    private ComputeServiceContext context;
    private final ComputeService client;

    public TestInfraStructure(ComputeServiceContext context,
                              ComputeService client,
                              Set<? extends NodeMetadata> nodes) {
      this.context = context;
      this.client = client;
      this.nodes = nodes.stream().map(JCloudsNode::new).collect(Collectors.toSet());
    }

    @Override
    public Set<Node> getNodes() {
      return Collections.unmodifiableSet(this.nodes);
    }

    @Override
    public CommandResult onNode(Node node, String[] shellCommand) throws IOException {
      NodeMetadata metadata = ((JCloudsNode) node).getMetadata();

      String script = String.join(" ", shellCommand);
      ExecResponse
          result =
          client.runScriptOnNode(metadata.getId(), script);

      if(result.getExitStatus() != 0) {
        throw new RuntimeException("Script execution failed. " + result.getError() + " " + result.getOutput());
      }

      System.out.println(result.getOutput());
      System.err.println(result.getError());

      return null;
    }

    @Override
    public void delete() {
      if(deleteAfterComplete) {
        nodes.forEach(node -> client.destroyNode(node.getMetadata().getId()));
      }
    }

    @Override
    public void copyToNodes(Iterable<File> files, String destDir) throws IOException {
      for(JCloudsNode node : nodes) {
        SshClient ssh = context.utils().sshForNode().apply(node.getMetadata());
        ssh.connect();
        ssh.exec("mkdir " + destDir);
        for(File file : files) {
          ssh.put(destDir + "/" + file.getName(), new FilePayload(file));
        }
      }
    }

    @Override
    public void copyFromNode(Node node, String directory, File destDir) {
      throw new IllegalStateException("Not yet implemented");
    }
  }

  public static class JCloudsNode implements Infrastructure.Node {
    public NodeMetadata getMetadata() {
      return metadata;
    }

    private final NodeMetadata metadata;

    public JCloudsNode(NodeMetadata metadata) {
      this.metadata = metadata;
    }

    @Override
    public InetAddress getAddress() {
      throw new RuntimeException("Not yet implemented!");
    }
  }
}
