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

import static org.apache.geode.benchmark.tasks.DefineHostNamingsOffPlatformTask.getOffPlatformHostName;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.perftest.TestContext;

public class StartServerSNI extends StartServer {

  private final int serverPortForSni;

  public StartServerSNI(final int locatorPort, final int serverPortForSni) {
    super(locatorPort);
    this.serverPortForSni = serverPortForSni;
  }

  @Override
  protected void configureCacheServer(final CacheServer cacheServer, final TestContext context)
      throws UnknownHostException {
    cacheServer.setMaxConnections(Integer.MAX_VALUE);
    cacheServer.setPort(serverPortForSni);
//    cacheServer.setHostnameForClients(
//        getOffPlatformHostName(context, InetAddress.getLocalHost()));
  }

}
