package org.apache.geode.benchmark.tasks;

import java.io.Serializable;
import java.util.Map;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class PutTask extends BenchmarkDriverAdapter implements Serializable {

  private Region<Object, Object> region;

  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);
    ClientCache cache = ClientCacheFactory.getAnyInstance();
    region = cache.getRegion("region");
  }

  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    region.put(1,2);
    return true;
  }
}
