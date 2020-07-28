package org.apache.geode.benchmark.topology;

import static org.apache.geode.benchmark.parameters.Utils.addToTestConfig;

import org.apache.geode.benchmark.parameters.GcLoggingParameters;
import org.apache.geode.benchmark.parameters.GcParameters;
import org.apache.geode.benchmark.parameters.HeapParameters;
import org.apache.geode.benchmark.parameters.JvmParameters;
import org.apache.geode.benchmark.parameters.ProfilerParameters;
import org.apache.geode.perftest.TestConfig;

public abstract class Topology {
  static final String WITH_SSL_PROPERTY = "withSsl";
  static final String WITH_SSL_ARGUMENT = "-DwithSsl=true";

  static final String WITH_SECURITY_MANAGER_PROPERTY = "withSecurityManager";
  static final String WITH_SECURITY_MANAGER_ARGUMENT = "-DwithSecurityManager=true";

  static void configureCommon(TestConfig config) {
    JvmParameters.configure(config);
    HeapParameters.configure(config);
    GcLoggingParameters.configure(config);
    GcParameters.configure(config);
    ProfilerParameters.configure(config);

    addToTestConfig(config, WITH_SSL_PROPERTY, WITH_SSL_ARGUMENT);
    addToTestConfig(config, WITH_SECURITY_MANAGER_PROPERTY, WITH_SECURITY_MANAGER_ARGUMENT);
  }


}
