package org.apache.geode.perftest.infrastructure.ssh;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.Infrastructure;

public class SshInfraManager implements InfraManager {

  private final Collection<String> hosts;
  private final String user;

  public SshInfraManager(String user, String ... hosts) {
    this.hosts = Arrays.asList(hosts);
    this.user = user;
  }

  @Override
  public Infrastructure create(int nodes) throws Exception {
    return new SshInfrastructure(hosts, user);
  }
}
