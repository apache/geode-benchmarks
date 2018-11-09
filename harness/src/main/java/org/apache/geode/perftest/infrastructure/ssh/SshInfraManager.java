package org.apache.geode.perftest.infrastructure.ssh;

import java.util.Collection;
import java.util.Collections;

import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.Infrastructure;

public class SshInfraManager implements InfraManager {
  @Override
  public Infrastructure create(int nodes) throws Exception {
    return new SshInfrastructure(Collections.emptySet());
  }
}
