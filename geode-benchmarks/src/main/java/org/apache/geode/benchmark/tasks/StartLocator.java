/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.benchmark.tasks;

import java.io.File;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.distributed.Locator;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to start the locator
 */
public class StartLocator implements Task {
  private int locatorPort;

  public StartLocator(int locatorPort) {
    this.locatorPort = locatorPort;
  }

  @Override
  public void run(TestContext context) throws Exception {
    Properties properties = new Properties();

    String statsFile = new File(context.getOutputDir(), "stats.gfs").getAbsolutePath();
    properties.setProperty(ConfigurationProperties.STATISTIC_SAMPLING_ENABLED, "true");
    properties.setProperty(ConfigurationProperties.STATISTIC_ARCHIVE_FILE, statsFile);
    properties.setProperty(ConfigurationProperties.ENABLE_CLUSTER_CONFIGURATION, "false");
    properties.setProperty(ConfigurationProperties.USE_CLUSTER_CONFIGURATION, "false");

    properties.setProperty(ConfigurationProperties.NAME, "locator-" + InetAddress.getLocalHost());
    Locator.startLocatorAndDS(locatorPort, null, properties);
  }
}
