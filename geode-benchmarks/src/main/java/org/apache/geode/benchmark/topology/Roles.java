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

import static org.apache.geode.benchmark.topology.RoleKinds.GEODE_PRODUCT;
import static org.apache.geode.benchmark.topology.RoleKinds.SUPPORTING;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * All roles defined for the JVMs created for the benchmark
 */
public enum Roles {
  SERVER(GEODE_PRODUCT),
  CLIENT(GEODE_PRODUCT),
  LOCATOR(GEODE_PRODUCT),
  PROXY(SUPPORTING),
  ROUTER(SUPPORTING);

  public final RoleKinds roleKind;

  public static Stream<Roles> rolesFor(final RoleKinds roleKind) {
    return Arrays.stream(Roles.values())
        .filter(role -> role.roleKind == roleKind);
  }

  Roles(final RoleKinds roleKind) {
    this.roleKind = roleKind;
  }
}
