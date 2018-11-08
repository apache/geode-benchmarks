package org.apache.geode.perftest;

import java.io.File;
import java.nio.charset.Charset;

import com.google.common.io.Files;
import org.apache.geode.perftest.infrastructure.jclouds.JCloudsInfraManager;

import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.junit.Test;

import org.apache.geode.perftest.infrastructure.local.LocalInfraManager;
import org.apache.geode.perftest.jvms.JVMManager;

public class TestRunnerIntegrationTest {

  @Test
  public void testBeforeWorkload() throws Exception {
    TestRunner runner = new TestRunner(new LocalInfraManager(), new JVMManager());

    runner.runTest(testConfig -> {
      testConfig.role("all", 2);
      testConfig.before(context -> System.out.println("hello"), "all");

    }, 1);

  }

  @Test
  public void testBeforeWorkloadInGCP() throws Exception {
    String json = Files.toString(
        new File("/Users/dsmith/Documents/Code/perf_tess/gemfire-dev-bea9bad8f611.json"),
        Charset.defaultCharset());
    GoogleCredentialsFromJson credentials = new GoogleCredentialsFromJson(json);
    TestRunner runner = new TestRunner(new JCloudsInfraManager("google-compute-engine", "yardstick-tester.*", credentials,
        "jclouds-test", false, false), new JVMManager());

    runner.runTest(testConfig -> {
      testConfig.role("all", 2);
      testConfig.before(context -> System.out.println("hello"), "all");

    }, 1);

  }
}
