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

package org.apache.geode.perftest.infrastructure;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Set;
import java.util.function.Function;

/**
 * Some deployed infrastructure that the test is running on
 */
public interface Infrastructure extends AutoCloseable {

  /**
   * Get the nodes that the test is running on
   */
  Set<Node> getNodes();


  int onNode(Node node, String[] shellCommand)
      throws IOException, InterruptedException;

  /**
   * Delete the nodes
   */
  void close() throws InterruptedException, IOException;

  /**
   * Copy a list of files to a directory on the node.
   *
   * @param files A list of files on the local system to copy
   * @param destDirectoryFunction A function that returns the destination directory, given a node
   * @param removeExisting If true, remove all existing files in the directory on the remote
   *        machine
   */
  void copyToNodes(Iterable<File> files, Function<Node, String> destDirectoryFunction,
      boolean removeExisting) throws IOException;

  void copyFromNode(Node node, String directory, File destDir) throws IOException;

  interface Node extends Serializable {

    InetAddress getAddress();
  }
}
