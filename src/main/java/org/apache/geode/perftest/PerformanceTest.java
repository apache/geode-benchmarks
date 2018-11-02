package org.apache.geode.perftest;

import java.io.Serializable;

public interface PerformanceTest extends Serializable {

  void configure(TestConfig test);

}
