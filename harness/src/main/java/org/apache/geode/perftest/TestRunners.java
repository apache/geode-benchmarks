/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.perftest;

import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.local.LocalInfraManager;
import org.apache.geode.perftest.infrastructure.ssh.SshInfraManager;
import org.apache.geode.perftest.jvms.JVMManager;

public class TestRunners {

  public static TestRunner defaultRunner() {
    String testHostsString = System.getenv("TEST_HOSTS");
    InfraManager infraManager;
    if(testHostsString == null) {
      infraManager = new LocalInfraManager();
    } else {
      String[] hosts = testHostsString.split(",");
      infraManager = new SshInfraManager(System.getProperty("user.name"), hosts);
    }

    return new TestRunner(infraManager, new JVMManager());
  }
}
