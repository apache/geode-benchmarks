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

package org.apache.geode.perftest.infrastructure.ssh;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.junit.rules.TemporaryFolder;

/**
 * Rule to run an in process ssh server during a test
 *
 * This ssh server listens on localhost. It does actually run commands and create
 * files on the real filesystem. It accepts connections from any user.
 */
public class SshServerRule extends TemporaryFolder {

  private SshServer sshd;

  @Override
  protected void before() throws Throwable {
    super.before();
    sshd = SshServer.setUpDefaultServer();
    sshd.setPort(0);
    sshd.setHost("localhost");
    sshd.setPublickeyAuthenticator((username, key, session) -> true);
    sshd.setKeyPairProvider(
        new SimpleGeneratorHostKeyProvider(Paths.get(newFolder().getPath(), "hostkey.ser")));
    sshd.setCommandFactory(new UnescapingCommandFactory());
    sshd.start();
  }

  public int getPort() {
    return sshd.getPort();
  }

  @Override
  protected void after() {
    try {
      sshd.stop();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private class UnescapingCommandFactory extends ProcessShellCommandFactory {
    @Override
    public Command createCommand(String command) {
      return super.createCommand(command.replace("'", ""));
    }
  }
}
