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

package org.apache.geode.perftest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.apache.geode.perftest.infrastructure.ssh.SshInfrastructureFactory;
import org.apache.geode.perftest.runner.DefaultTestRunner;

public class TestRunnersTest {

  @Test
  public void defaultRunnerShouldParseHosts() {
    DefaultTestRunner runner =
        (DefaultTestRunner) TestRunners.defaultRunner("localhost,localhost", null);

    SshInfrastructureFactory infrastructureFactory =
        (SshInfrastructureFactory) runner.getRemoteJvmFactory().getInfrastructureFactory();

    assertEquals(Arrays.asList("localhost", "localhost"), infrastructureFactory.getHosts());
  }

  @Test
  public void defaultRunnerShouldFailWithNoHosts() {

    assertThrows(IllegalStateException.class, () -> TestRunners.defaultRunner(null, null));
  }
}
