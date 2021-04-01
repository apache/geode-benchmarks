package org.apache.geode.perftest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkProperties {

  public static Map<String, List<String>> getDefaultJVMArgs() {
    Map<String, List<String>> results = new HashMap<>();
    System.getProperties().stringPropertyNames().stream()
        .filter(name -> name.startsWith("benchmark.system."))
        .forEach(name -> {
          String shortName = name.replace("benchmark.system.", "");
          String[] roleAndProperty = shortName.split("\\.", 2);
          String role = roleAndProperty[0];
          String property = roleAndProperty[1];
          String value = System.getProperty(name);
          List<String> roleProperties = results.computeIfAbsent(role, key -> new ArrayList<>());
          roleProperties.add("-D" + property + "=" + value);
        });
    return results;
  }
}
