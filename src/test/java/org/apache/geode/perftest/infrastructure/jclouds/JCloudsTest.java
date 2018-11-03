package org.apache.geode.perftest.infrastructure.jclouds;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.junit.Test;

public class JCloudsTest {

  @Test
  public void runCommandInGCP() throws IOException, RunNodesException {
    String json = Files.toString(
        new File("/Users/dsmith/Documents/Code/perf_tess/gemfire-dev-bea9bad8f611.json"),
        Charset.defaultCharset());
    GoogleCredentialsFromJson credentials = new GoogleCredentialsFromJson(json);


    launchRunAndShutdown(credentials,
        "google-compute-engine",
        "yardstick-tester.*",
        "us-central1-c",
        "hostname");
  }

  @Test
  public void runCommandLocally() throws IOException, RunNodesException {


    Supplier<Credentials> credentials = () -> new Credentials("username", "password");
    launchRunAndShutdown(credentials,
        "stub",
        "UBUNTU",
        "stub",
        "hostname");
  }

  private void launchRunAndShutdown(Supplier<Credentials> credentials, String provider,
                                    String imageNameRegex, String zone, String script) throws IOException, RunNodesException {

    ComputeServiceContext context = ContextBuilder.newBuilder(provider)
        .modules(ImmutableSet.<Module> of(new SshjSshClientModule() ))
        .credentialsSupplier(credentials)
        .buildView(ComputeServiceContext.class);

    ComputeService client = context.getComputeService();

    Template template = client.templateBuilder()
            .imageNameMatches(imageNameRegex)
            .locationId(zone)
            .minCores(8)
            .build();

    NodeMetadata node = client.createNodesInGroup("jclcouds-tester", 1, template).iterator().next();

    ExecResponse result = client.runScriptOnNode(node.getId(), script);

    System.out.println(result.getOutput());

    client.destroyNode(node.getId());
  }

}
