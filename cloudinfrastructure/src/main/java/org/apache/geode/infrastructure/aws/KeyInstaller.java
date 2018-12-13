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

package org.apache.geode.infrastructure.aws;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FilePermission;
import net.schmizz.sshj.xfer.FileSystemFile;

public class KeyInstaller {
  public static final Config CONFIG = new DefaultConfig();
  private final String user;
  private final Path privateKey;

  public KeyInstaller(String user, Path privateKey) {
    this.user = user;
    this.privateKey = privateKey;
  }


  public void installPrivateKey(Collection<String> hosts) {
    hosts.forEach(this::installKey);
  }

  private void installKey(String host) {
    try (SSHClient client = new SSHClient(CONFIG)) {
      client.addHostKeyVerifier(new PromiscuousVerifier());
      client.connect(host);
      client.authPublickey(user, privateKey.toFile().getAbsolutePath());
      SFTPClient sftpClient = client.newSFTPClient();
      String dest = "/home/" + user + "/.ssh/id_rsa";
      sftpClient.put(new FileSystemFile(privateKey.toFile()), dest);
      sftpClient.setattr(dest, new FileAttributes.Builder()
          .withPermissions(Collections.singleton(FilePermission.USR_R)).build());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
