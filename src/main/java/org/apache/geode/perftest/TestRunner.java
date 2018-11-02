package org.apache.geode.perftest;

import java.util.List;
import java.util.Map;

import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.infrastructure.local.LocalInfraManager;
import org.apache.geode.perftest.jvms.JVMManager;
import org.apache.geode.perftest.jvms.RemoteJVMs;

public class TestRunner {

  private InfraManager infraManager = new LocalInfraManager();
  private JVMManager jvmManager = new JVMManager();

  public void runTest(PerformanceTest test, int nodes) throws Exception {
    Infrastructure infra = infraManager.create(nodes);

    try {
      TestConfig config = new TestConfig();
      test.configure(config);

      Map<String, Integer> roles = config.getRoles();

      //Map roles to nodes and get number of JVMs per node

      //launch JVMs in parallel, hook them up
      RemoteJVMs remoteJVMs = jvmManager.launch(infra, roles);

      runTasks(config.getBefore(), remoteJVMs);
      runTasks(config.getWorkload(), remoteJVMs);
      runTasks(config.getAfter(), remoteJVMs);
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
