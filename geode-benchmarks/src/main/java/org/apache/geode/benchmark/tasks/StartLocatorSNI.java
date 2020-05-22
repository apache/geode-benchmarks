package org.apache.geode.benchmark.tasks;

import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.getOffPlatformHostName;

import java.io.IOException;
import java.util.Properties;

import org.apache.geode.distributed.LocatorLauncher;
import org.apache.geode.perftest.TestContext;

public class StartLocatorSNI extends StartLocator {

  public StartLocatorSNI(final int locatorPort) {
    super(locatorPort);
  }

  @Override
  protected void startLocator(final Properties properties, final int locatorPort,
                                 final TestContext context) throws IOException {
    new LocatorLauncher.Builder()
        .set(properties)
        .setPort(locatorPort)
        .setHostnameForClients(getOffPlatformHostName(context))
        .build()
        .start();
  }

}
