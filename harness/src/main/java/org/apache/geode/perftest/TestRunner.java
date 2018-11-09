package org.apache.geode.perftest;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.jvms.JVMManager;
import org.apache.geode.perftest.jvms.RemoteJVMs;

/**
 * Runner that executes a {@link PerformanceTest}, using
 * a provided {@link InfraManager}.
 *
 * This is the main entry point for running tests. Users should
 * implement {@link PerformanceTest} to define there tests in
 * a declarative fashion and then execute them this runner.
 */
public class TestRunner {
  Logger logger = LoggerFactory.getLogger(TestRunner.class);


  private final InfraManager infraManager;
  private final JVMManager jvmManager;

  public TestRunner(InfraManager infraManager, JVMManager jvmManager) {
    this.infraManager = infraManager;
    this.jvmManager = jvmManager;
  }

  public void runTest(PerformanceTest test, int nodes) throws Exception {
    Infrastructure infra = infraManager.create(nodes);

    try {
      TestConfig config = new TestConfig();
      test.configure(config);

      Map<String, Integer> roles = config.getRoles();

      logger.info("Lauching JVMs...");
      //launch JVMs in parallel, hook them up
      RemoteJVMs remoteJVMs = jvmManager.launch(infra, roles);

      logger.info("Starting before tasks...");
      runTasks(config.getBefore(), remoteJVMs);

      logger.info("Starting workload tasks...");
      runTasks(config.getWorkload(), remoteJVMs);

      logger.info("Starting after tasks...");
      runTasks(config.getAfter(), remoteJVMs);

      logger.info("Copying results...");
      File outputDir = new File("output");
      int nodeId = 0;
      for(Infrastructure.Node node : infra.getNodes()) {
        infra.copyFromNode(node, "output", new File(outputDir, "node-" + nodeId++));
      }

    } finally {
      infra.delete();
    }
  }

  private void runTasks(List<TestConfig.TestStep> steps,
                        RemoteJVMs remoteJVMs) {
    steps.forEach(testStep -> {
      remoteJVMs.execute(testStep.getTask(), testStep.getRoles());
    });
  }
}
