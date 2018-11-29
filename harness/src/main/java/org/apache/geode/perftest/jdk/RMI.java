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

package org.apache.geode.perftest.jdk;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Wrapper around the JDKs RMI interface, which is static, and can't be mocked.
 *
 * Using this instead of Java RMI directly allows us to unit test classes that
 * interact with RMI.
 */
public class RMI {

  /**
   * Wrapper around {@link Naming#lookup(String)}
   */
  public Remote lookup(String name)
      throws RemoteException, NotBoundException, MalformedURLException {
    return Naming.lookup(name);
  }

  /**
   * Wrapper around {@link LocateRegistry#createRegistry(int)}
   */
  public Registry createRegistry(int rmiPort) throws RemoteException {
    return LocateRegistry.createRegistry(rmiPort);
  }
}
