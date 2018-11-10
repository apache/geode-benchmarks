package org.apache.geode.perftest;

import org.apache.geode.perftest.infrastructure.ssh.SshInfraManager;
import org.apache.geode.perftest.jvms.JVMManager;

public class TestRunners {

  public static TestRunner defaultRunner() {
    String[] hosts= System.getenv("TEST_HOSTS").split(",");

    return new TestRunner(new SshInfraManager(System.getProperty("user.name"), hosts), new JVMManager());
  }
}
