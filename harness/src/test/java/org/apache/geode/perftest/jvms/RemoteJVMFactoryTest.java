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

package org.apache.geode.perftest.jvms;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.infrastructure.InfrastructureFactory;
import org.apache.geode.perftest.jvms.classpath.ClassPathCopier;
import org.apache.geode.perftest.jvms.rmi.Controller;
import org.apache.geode.perftest.jvms.rmi.ControllerFactory;

public class RemoteJVMFactoryTest {

  private JVMLauncher jvmLauncher;
  private ClassPathCopier classPathCopier;
  private RemoteJVMFactory factory;
  private Controller controller;
  private ControllerFactory controllerFactory;
  private Infrastructure infra;

  @Before
  public void setUp() throws AlreadyBoundException, RemoteException {
    classPathCopier = mock(ClassPathCopier.class);
    jvmLauncher = mock(JVMLauncher.class);
    controller = mock(Controller.class);
    controllerFactory = mock(ControllerFactory.class);
    when(controllerFactory.createController(any(), anyInt())).thenReturn(controller);
    infra = mock(Infrastructure.class);
    InfrastructureFactory infraFactory = nodes -> infra;
    factory = new RemoteJVMFactory(infraFactory, jvmLauncher, classPathCopier, controllerFactory);
  }

  @Test
  public void launchMethodCreatesControllerAndLaunchesNodes() throws Exception {
    Map<String, Integer> roles = Collections.singletonMap("role", 2);

    Infrastructure.Node node1 = mock(Infrastructure.Node.class);
    Infrastructure.Node node2 = mock(Infrastructure.Node.class);
    Set<Infrastructure.Node> nodes = Stream.of(node1, node2).collect(Collectors.toSet());
    when(infra.getNodes()).thenReturn(nodes);

    when(controller.waitForWorkers(anyInt(), any())).thenReturn(true);

    factory.launch(roles, Collections.emptyMap());

    InOrder inOrder = inOrder(controller, controllerFactory, jvmLauncher, classPathCopier, infra);

    inOrder.verify(controllerFactory).createController(any(), eq(2));
    inOrder.verify(jvmLauncher).launchProcesses(eq(infra), anyInt(), any(), any());
    inOrder.verify(controller).waitForWorkers(anyInt(), any());



  }

}
