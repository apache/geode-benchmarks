package org.apache.geode.perftest.infrastructure.jclouds;

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