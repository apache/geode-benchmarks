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

package org.apache.geode.perftest.runner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.jvms.JVMMapping;

public class SharedContextTest {

  @Test
  public void getHostsForRoleShouldReturnCorrectList() throws UnknownHostException {

    InetAddress host1 = InetAddress.getByName("127.0.0.1");
    InetAddress host2 = InetAddress.getByName("127.0.0.2");
    Infrastructure.Node node1 = mock(Infrastructure.Node.class);
    when(node1.getAddress()).thenReturn(host1);
    JVMMapping mapping1 = new JVMMapping(node1, "role", 1, Collections.emptyList());
    Infrastructure.Node node2 = mock(Infrastructure.Node.class);
    when(node2.getAddress()).thenReturn(host2);
    JVMMapping mapping2 = new JVMMapping(node2, "role", 2, Collections.emptyList());

    SharedContext context = new SharedContext(Arrays.asList(mapping1, mapping2));

    Set<InetAddress> hosts = context.getHostsForRole("role");

    assertEquals(Stream.of(host1, host2).collect(Collectors.toSet()), hosts);

  }

}
