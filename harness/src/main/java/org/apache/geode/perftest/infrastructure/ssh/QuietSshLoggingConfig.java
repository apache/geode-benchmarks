package org.apache.geode.perftest.infrastructure.ssh;

import net.schmizz.sshj.DefaultConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

class QuietSshLoggingConfig extends DefaultConfig {
  private static final Logger logger = LoggerFactory.getLogger(QuietSshLoggingConfig.class);

  @Override
  public net.schmizz.sshj.common.LoggerFactory getLoggerFactory() {
    if (logger.isDebugEnabled()) {
      return super.getLoggerFactory();
    } else {
      return new NOPLoggerFactory();
    }
  }

  private static class NOPLoggerFactory implements net.schmizz.sshj.common.LoggerFactory {
    @Override
    public Logger getLogger(String name) {
      return NOPLogger.NOP_LOGGER;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
      return NOPLogger.NOP_LOGGER;
    }
  }
}
