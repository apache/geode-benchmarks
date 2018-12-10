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
