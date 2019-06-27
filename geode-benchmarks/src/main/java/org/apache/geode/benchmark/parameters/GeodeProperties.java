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

import static org.apache.geode.distributed.ConfigurationProperties.ARCHIVE_DISK_SPACE_LIMIT;
import static org.apache.geode.distributed.ConfigurationProperties.ARCHIVE_FILE_SIZE_LIMIT;
import static org.apache.geode.distributed.ConfigurationProperties.CONSERVE_SOCKETS;
import static org.apache.geode.distributed.ConfigurationProperties.DISTRIBUTED_SYSTEM_ID;
import static org.apache.geode.distributed.ConfigurationProperties.ENABLE_CLUSTER_CONFIGURATION;
import static org.apache.geode.distributed.ConfigurationProperties.ENABLE_TIME_STATISTICS;
import static org.apache.geode.distributed.ConfigurationProperties.LOCATOR_WAIT_TIME;
import static org.apache.geode.distributed.ConfigurationProperties.LOG_DISK_SPACE_LIMIT;
import static org.apache.geode.distributed.ConfigurationProperties.LOG_FILE_SIZE_LIMIT;
import static org.apache.geode.distributed.ConfigurationProperties.LOG_LEVEL;
import static org.apache.geode.distributed.ConfigurationProperties.MEMBER_TIMEOUT;
import static org.apache.geode.distributed.ConfigurationProperties.REMOVE_UNRESPONSIVE_CLIENT;
import static org.apache.geode.distributed.ConfigurationProperties.SERIALIZABLE_OBJECT_FILTER;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_ENABLED_COMPONENTS;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_KEYSTORE;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_KEYSTORE_PASSWORD;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_TRUSTSTORE;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_TRUSTSTORE_PASSWORD;
import static org.apache.geode.distributed.ConfigurationProperties.STATISTIC_SAMPLING_ENABLED;
import static org.apache.geode.distributed.ConfigurationProperties.USE_CLUSTER_CONFIGURATION;
import static org.apache.geode.security.SecurableCommunicationChannels.ALL;

import java.util.Properties;

import org.apache.geode.distributed.ConfigurationProperties;

public class GeodeProperties {

  public static Properties serverProperties() {
    Properties properties = new Properties();

    properties.setProperty(CONSERVE_SOCKETS, "false");
    properties.setProperty(ENABLE_TIME_STATISTICS, "true");
    properties.setProperty(LOCATOR_WAIT_TIME, "120");
    properties.setProperty(LOG_DISK_SPACE_LIMIT, "100");
    properties.setProperty(LOG_FILE_SIZE_LIMIT, "10");
    properties.setProperty(LOG_LEVEL, "config");
    properties.setProperty(REMOVE_UNRESPONSIVE_CLIENT, "true");
    properties.setProperty(STATISTIC_SAMPLING_ENABLED, "true");
    properties.setProperty(ARCHIVE_DISK_SPACE_LIMIT, "150");
    properties.setProperty(ARCHIVE_FILE_SIZE_LIMIT, "10");
    properties.setProperty(DISTRIBUTED_SYSTEM_ID, "0");
    properties.setProperty(ENABLE_CLUSTER_CONFIGURATION, "false");
    properties.setProperty(USE_CLUSTER_CONFIGURATION, "false");
    properties.setProperty(SERIALIZABLE_OBJECT_FILTER, "benchmark.geode.data.**");

    return properties;
  }

  public static Properties locatorProperties() {
    // Locator properties are the same as the server properties right now
    return serverProperties();
  }

  public static Properties clientProperties() {
    Properties properties = new Properties();

    properties.setProperty(ENABLE_TIME_STATISTICS, "true");
    properties.setProperty(LOG_LEVEL, "config");
    properties.setProperty(STATISTIC_SAMPLING_ENABLED, "true");
    properties.setProperty(MEMBER_TIMEOUT, "8000");

    return properties;
  }


  public static Properties withSsl(Properties properties) {
    //properties.setProperty(SSL_ENABLED_COMPONENTS, ALL);

    // TODO make configurable, relative, something?
    properties.setProperty(SSL_KEYSTORE, "/home/geode/selfsigned.jks");
    properties.setProperty(SSL_KEYSTORE_PASSWORD, "123456");
    properties.setProperty(SSL_TRUSTSTORE, "/home/geode/selfsigned.jks");
    properties.setProperty(SSL_TRUSTSTORE_PASSWORD, "123456");

    return properties;
  }
}
