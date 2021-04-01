package org.apache.geode.perftest;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class BenchmarkPropertiesTest {

  @Test
  public void canParsePropertiesForRoles() {
    System.setProperty("benchmark.system.role1.p1", "v1");
    System.setProperty("benchmark.system.role1.p2", "v2");

    Map<String, List<String>> defaultArgs =
        BenchmarkProperties.getDefaultJVMArgs();

    Assertions.assertThat(defaultArgs).containsOnlyKeys("role1");

    Assertions.assertThat(defaultArgs.get("role1")).containsExactlyInAnyOrder("-Dp1=v1", "-Dp2=v2");
  }

}
