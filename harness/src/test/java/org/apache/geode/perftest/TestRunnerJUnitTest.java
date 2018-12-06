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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InOrder;

import org.apache.geode.perftest.jvms.RemoteJVMFactory;
import org.apache.geode.perftest.jvms.RemoteJVMs;
import org.apache.geode.perftest.runner.DefaultTestRunner;

public class TestRunnerJUnitTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testRunnerRunsBeforeAndAfterTasks() throws Exception {

    RemoteJVMFactory remoteJvmFactory = mock(RemoteJVMFactory.class);

    RemoteJVMs remoteJVMs = mock(RemoteJVMs.class);
    when(remoteJvmFactory.launch(any(), any())).thenReturn(remoteJVMs);

    TestRunner runner = new DefaultTestRunner(remoteJvmFactory,
        folder.newFolder());

    Task before = mock(Task.class);
    Task after = mock(Task.class);

    PerformanceTest test = () -> {
      TestConfig config = new TestConfig();
      config.name("SampleBenchmark");
      config.role("before", 1);
      config.role("workload", 1);
      config.role("after", 1);

      config.before(before, "before");
      config.after(after, "before");
      return config;
    };
    runner.runTest(test);

    InOrder inOrder = inOrder(remoteJVMs, before, after);
    inOrder.verify(remoteJVMs).execute(eq(before), any());
    inOrder.verify(remoteJVMs).execute(eq(after), any());
  }

  @Test
  public void requiresAtLeastOneRole() throws Exception {

    RemoteJVMFactory remoteJvmFactory = mock(RemoteJVMFactory.class);

    RemoteJVMs remoteJVMs = mock(RemoteJVMs.class);
    when(remoteJvmFactory.launch(any(), any())).thenReturn(remoteJVMs);

    TestRunner runner = new DefaultTestRunner(remoteJvmFactory,
        folder.newFolder());

    Task before = mock(Task.class);

    PerformanceTest test = () -> {
      TestConfig config = new TestConfig();
      config.name("SampleBenchmark");
      config.role("before", 1);

      config.before(before);
      return config;
    };
    Assertions.assertThatThrownBy(() -> runner.runTest(test))
        .isInstanceOf(IllegalStateException.class);
  }
}
