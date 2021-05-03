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
package org.apache.geode.benchmark.tasks;

import static java.lang.String.format;
import static java.lang.String.join;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessControl {
  private static final Duration RETRY_TIMEOUT = Duration.ofMinutes(1);

  private static final Logger logger = LoggerFactory.getLogger(ProcessControl.class);

  public static void runCommand(final String command) throws IOException, InterruptedException {
    final Process startDaemon = Runtime.getRuntime().exec(command);
    final int exitStatus = startDaemon.waitFor();
    if (exitStatus != 0) {
      final String msg = format("'%s' command exited with status %d\npwd is: %s", command,
          exitStatus, System.getProperty("user.dir"));
      logger.error(msg);
      throw new IllegalStateException(msg);
    }
  }

  public static void runAndExpectZeroExit(final ProcessBuilder processBuilder)
      throws IOException, InterruptedException {
    final Process process = processBuilder.start();
    final int exitStatus = process.waitFor();
    if (exitStatus != 0) {
      final String msg =
          format("'%s' command exited with status %d", join(" ", processBuilder.command()),
              exitStatus, System.getProperty("user.dir"));
      logger.error(msg);
      throw new IllegalStateException(msg);
    }
  }

  public static void retryUntilZeroExit(final ProcessBuilder processBuilder)
      throws IOException, InterruptedException {
    long start = System.nanoTime();
    while (true) {
      final Process process = processBuilder.start();
      final int exitStatus = process.waitFor();
      if (exitStatus != 0) {
        final String msg =
            format("'%s' command exited with status %d", join(" ", processBuilder.command()),
                exitStatus, System.getProperty("user.dir"));
        logger.error(msg);
        if (System.nanoTime() - start > RETRY_TIMEOUT.toNanos()) {
          throw new RuntimeException(msg);
        }
        Thread.sleep(100);
        continue;
      }
      break;
    }
  }

}
