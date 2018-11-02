package org.apache.geode.perftest.infrastructure.local;

import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.Infrastructure;

public class LocalInfraManager implements InfraManager {
  @Override
  public Infrastructure create(int nodes) throws Exception {
    return new LocalInfrastructure();
  }
}
