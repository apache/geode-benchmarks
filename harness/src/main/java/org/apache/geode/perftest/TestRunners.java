package org.apache.geode.perftest;

import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.local.LocalInfraManager;
import org.apache.geode.perftest.infrastructure.ssh.SshInfraManager;
import org.apache.geode.perftest.jvms.JVMManager;

public class TestRunners {

  public static TestRunner defaultRunner() {
    String testHostsString = System.getenv("TEST_HOSTS");
    InfraManager infraManager;
    if(testHostsString == null) {
      infraManager = new LocalInfraManager();
    } else {
      String[] hosts = testHostsString.split(",");
      infraManager = new SshInfraManager(System.getProperty("user.name"), hosts);
    }

    return new TestRunner(infraManager, new JVMManager());
  }
}
