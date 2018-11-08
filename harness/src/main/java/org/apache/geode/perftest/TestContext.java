package org.apache.geode.perftest;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.geode.perftest.jvms.JVMManager;

public class TestContext implements Serializable {

  private List<JVMManager.JVMMapping> jvmMappings;

  public TestContext(List<JVMManager.JVMMapping> jvmMappings) {

    this.jvmMappings = jvmMappings;
  }

  public Set<InetAddress> getHostsForRole(String role) {
    return jvmMappings.stream()
        .filter(mapping -> mapping.getRole().equals(role))
        .map(mapping -> mapping.getNode().getAddress())
        .collect(Collectors.toSet());
  }
}
