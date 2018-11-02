package infrastructure.jclouds;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;
import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.junit.Test;

public class JCloudsInfraManagerTest {

//  @Test
//  public void testStub() throws RunNodesException, IOException {
//    JCloudsInfraManager manager = new JCloudsInfraManager("stub", "ubuntu", () -> new Credentials("", ""));
//
//    Infrastructure infra = manager.create(nodes);
//
//    NodeMetadata node0 = infra.getNodes().iterator().next();
//
//    String hostname = infra.onNode(node0, "hostname");
//
//    System.out.println(hostname);
//
//    infra.delete();
//  }
//
//  @Test
//  public void testGCP() throws RunNodesException, IOException {
//    String
//        json =
//        Files.toString(
//            new File("/Users/dsmith/Documents/Code/perf_tess/gemfire-dev-bea9bad8f611.json"),
//            Charset
//                .defaultCharset());
//    GoogleCredentialsFromJson credentials = new GoogleCredentialsFromJson(json);
//
//    JCloudsInfraManager manager = new JCloudsInfraManager("google-compute-engine", "yardstick-tester",credentials);
//
//    System.out.println("Creating instance");
//    Infrastructure infra = manager.create(nodes);
//
//    NodeMetadata node0 = infra.getNodes().iterator().next();
//
//    System.out.println("Running command on " + node0);
//    String hostname = infra.onNode(node0, "hostname");
//
//    System.out.println(hostname);
//
//    infra.delete();
//  }

}