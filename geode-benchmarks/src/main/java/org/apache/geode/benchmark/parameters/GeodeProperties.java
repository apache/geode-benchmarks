/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.benchmark.parameters;

import java.util.Properties;

import org.apache.geode.distributed.ConfigurationProperties;

public class GeodeProperties {
  public static Properties serverProperties() {
    Properties serverProperties = new Properties();


    serverProperties.setProperty(ConfigurationProperties.CONSERVE_SOCKETS, "false");
    serverProperties.setProperty(ConfigurationProperties.ENABLE_TIME_STATISTICS, "true");
    serverProperties.setProperty(ConfigurationProperties.LOCATOR_WAIT_TIME, "120");
    serverProperties.setProperty(ConfigurationProperties.LOG_DISK_SPACE_LIMIT, "100");
    serverProperties.setProperty(ConfigurationProperties.LOG_FILE_SIZE_LIMIT, "10");
    serverProperties.setProperty(ConfigurationProperties.LOG_LEVEL, "config");
    serverProperties.setProperty(ConfigurationProperties.REMOVE_UNRESPONSIVE_CLIENT, "true");
    serverProperties.setProperty(ConfigurationProperties.STATISTIC_SAMPLING_ENABLED, "true");
    serverProperties.setProperty(ConfigurationProperties.ARCHIVE_DISK_SPACE_LIMIT, "150");
    serverProperties.setProperty(ConfigurationProperties.ARCHIVE_FILE_SIZE_LIMIT, "10");
    serverProperties.setProperty(ConfigurationProperties.DISTRIBUTED_SYSTEM_ID, "0");
    serverProperties.setProperty(ConfigurationProperties.ENABLE_CLUSTER_CONFIGURATION, "true");
    serverProperties.setProperty(ConfigurationProperties.USE_CLUSTER_CONFIGURATION, "true");
    return serverProperties;
  }

}
