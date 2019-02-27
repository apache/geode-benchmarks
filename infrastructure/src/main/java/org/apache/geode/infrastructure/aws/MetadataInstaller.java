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
import java.net.ConnectException;
import java.nio.file.Path;
import java.nio.file.Paths;
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


public class MetadataInstaller {
  public static final Config CONFIG = new DefaultConfig();
  private static final int RETRIES = 30;
  private final String user;
  private final Path metadata;
  private final Path privateKey;

  public MetadataInstaller(String benchmarkTag) {
    this.user = AwsBenchmarkMetadata.USER;
    this.privateKey = Paths.get(AwsBenchmarkMetadata.keyPairFileName(benchmarkTag));
    this.metadata = Paths.get(AwsBenchmarkMetadata.metadataFileName(benchmarkTag));
  }

  public void installMetadata(Collection<String> hosts) {
    hosts.forEach(this::installMetadata);
  }

  private void installMetadata(String host) {
    try (SSHClient client = new SSHClient(CONFIG)) {
      client.addHostKeyVerifier(new PromiscuousVerifier());
      connect(host, client);
      client.authPublickey(user, privateKey.toFile().getAbsolutePath());
      SFTPClient sftpClient = client.newSFTPClient();
      String dest = "/home/" + user + "/geode-benchmarks-metadata.json";

      sftpClient.put(new FileSystemFile(metadata.toFile()), dest);
      sftpClient.setattr(dest, new FileAttributes.Builder()
          .withPermissions(Collections.singleton(FilePermission.USR_R)).build());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void connect(String host, SSHClient client) throws IOException, InterruptedException {

    int i = 0;
    while (true) {
      try {
        i++;
        client.connect(host);
        return;
      } catch (ConnectException e) {
        if (i >= RETRIES) {
          throw e;
        }

        System.out.println("Failed to connect, retrying...");
        Thread.sleep(AwsBenchmarkMetadata.POLL_INTERVAL);
      }
    }
  }
}
