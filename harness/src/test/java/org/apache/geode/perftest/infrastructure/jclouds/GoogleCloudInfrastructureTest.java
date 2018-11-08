package org.apache.geode.perftest.infrastructure.jclouds;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import org.apache.geode.perftest.infrastructure.CommandResult;
import org.apache.geode.perftest.infrastructure.Infrastructure;

public class GoogleCloudInfrastructureTest {

  @Test
  public void canFindNodes() throws IOException {
    GoogleCloudInfrastructure infra = new GoogleCloudInfrastructure(1);

    assertEquals(1, infra.getNodes().size());
  }

  @Test
  public void canExecuteACommandOnNode()
      throws IOException, ExecutionException, InterruptedException {
    GoogleCloudInfrastructure infra = new GoogleCloudInfrastructure(1);
    Infrastructure.Node node1 = infra.getNodes().iterator().next();

    CommandResult result = infra.onNode(node1, new String[] {"echo", "hello"});

    assertEquals(0, result.getExitStatus());
    assertEquals("hello", result.getOutput());
  }

}