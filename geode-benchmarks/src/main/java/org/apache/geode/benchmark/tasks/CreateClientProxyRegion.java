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

import static org.apache.geode.benchmark.configurations.BenchmarkParameters.CLIENT_CACHE;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

/**
 * Task to create a PROXY region in the client
 */
public class CreateClientProxyRegion implements Task {
  @Override
  public void run(TestContext context) throws Exception {
    ClientCache clientCache = (ClientCache) context.getAttribute(CLIENT_CACHE);
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY)
        .create("region");

  }
}
