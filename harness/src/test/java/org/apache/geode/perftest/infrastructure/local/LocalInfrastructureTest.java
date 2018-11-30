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

package org.apache.geode.perftest.infrastructure.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LocalInfrastructureTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private LocalInfrastructure infra;
  private LocalInfrastructure.LocalNode node;

  @Before
  public void createInfra() throws IOException {
    infra = new LocalInfrastructure(1);
    node = (LocalInfrastructure.LocalNode) infra.getNodes().iterator().next();
  }

  @After
  public void deleteInfra() throws IOException, InterruptedException {
    infra.close();
  }


  @Test
  public void copyToNodesPutsFileOnNode() throws IOException, InterruptedException {

    File nodedir = node.workingDir;

    File someFile = temporaryFolder.newFile();

    File expectedDir = new File(nodedir, "lib");
    assertFalse(expectedDir.exists());
    infra.copyToNodes(Arrays.asList(someFile), "lib", true);
    assertTrue(expectedDir.exists());
    assertTrue(new File(expectedDir, someFile.getName()).exists());


    infra.close();

    assertFalse(expectedDir.exists());
  }

  @Test
  public void onNodeExecutesShellCommand()
      throws IOException, InterruptedException, ExecutionException {
    File nodedir = node.workingDir;

    File expectedFile = new File(nodedir, "tmpFile");
    assertFalse(expectedFile.exists());

    int result = infra.onNode(node, new String[] {"touch", "tmpFile"});

    assertEquals(0, result);

    expectedFile.exists();
  }

  @Test
  public void copyFromNodeCopiesFileFromNode() throws IOException {

    File newFile = new File(node.workingDir, "someFile");
    newFile.createNewFile();

    File destDirectory = temporaryFolder.newFolder();
    infra.copyFromNode(node, ".", destDirectory);

    assertTrue(new File(destDirectory, "someFile").exists());

  }

}
