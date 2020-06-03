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
package org.apache.geode.benchmark.topology;

public class Ports {
  /**
   * The port used to create the locator for the tests
   */
  public static final int LOCATOR_PORT = 10334;

  /**
   * With an SNI proxy, both the locator ports and the server ports
   * have to be well-known (static) since the proxy has to know them
   * and, in general, SNI proxies don't have visibility into locator
   * responses carrying server port numbers.
   */
  public static final int SERVER_PORT_FOR_SNI = 40404;

  public static final int SNI_PROXY_PORT = 15443;
}
