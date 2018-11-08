package org.apache.geode.perftest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;

import org.apache.geode.perftest.infrastructure.InfraManager;
import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.jvms.JVMManager;
import org.apache.geode.perftest.jvms.RemoteJVMs;

public class TestRunnerJUnitTest {

  @Test
  public void testRunnerRunsBeforeAndAfterTasks() throws Exception {

    InfraManager infraManager = mock(InfraManager.class);
    Infrastructure infrastructure = mock(Infrastructure.class);
    JVMManager jvmManager = mock(JVMManager.class);

    when(infraManager.create(anyInt())).thenReturn(infrastructure);

    RemoteJVMs remoteJVMs = mock(RemoteJVMs.class);
    when(jvmManager.launch(eq(infrastructure), any())).thenReturn(remoteJVMs);

    TestRunner runner = new TestRunner(infraManager, jvmManager);

    Task before = mock(Task.class);
    Task after = mock(Task.class);

    PerformanceTest test = config -> {
      config.role("before", 1);
      config.role("workload", 1);
      config.role("after", 1);

      config.before(before, "before");
      config.after(after, "before");
    };
    runner.runTest(test, 3);

    InOrder inOrder = inOrder(remoteJVMs, before, after);
    inOrder.verify(remoteJVMs).execute(eq(before), any());
    inOrder.verify(remoteJVMs).execute(eq(after), any());
  }

}