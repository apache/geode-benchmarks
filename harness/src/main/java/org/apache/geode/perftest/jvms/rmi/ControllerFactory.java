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

package org.apache.geode.perftest.jvms.rmi;

import static org.apache.geode.perftest.jvms.RemoteJVMFactory.CONTROLLER;
import static org.apache.geode.perftest.jvms.RemoteJVMFactory.RMI_PORT;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.apache.geode.perftest.jdk.RMI;
import org.apache.geode.perftest.runner.SharedContext;

public class ControllerFactory {

  private final RMI rmi = new RMI();

  public Controller createController(SharedContext sharedContext,
      int numWorkers) throws RemoteException, AlreadyBoundException {
    Registry registry = rmi.createRegistry(RMI_PORT);
    Controller controller = new Controller(numWorkers, registry, sharedContext);
    registry.bind(CONTROLLER, controller);
    return controller;
  }
}
