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

import java.io.File;
import java.io.PrintStream;
import java.rmi.RemoteException;

import org.apache.commons.io.FileUtils;

import org.apache.geode.perftest.jdk.RMI;
import org.apache.geode.perftest.jdk.SystemInterface;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;
import org.apache.geode.perftest.runner.DefaultTestContext;
import org.apache.geode.perftest.runner.SharedContext;

/**
 * Main method for a JVM running on a remote node
 */
public class ChildJVM {


  private final RMI rmi;
  private final SystemInterface system;
  private final int pingTime;

  public ChildJVM(RMI rmi, SystemInterface system, int pingTime) {
    this.rmi = rmi;
    this.system = system;
    this.pingTime = pingTime;
  }

  public static void main(String[] args) {
    new ChildJVM(new RMI(), new SystemInterface(), 1000).run();
  }

  void run() {
    try {
      String RMI_HOST = system.getProperty(RemoteJVMFactory.RMI_HOST);
      String RMI_PORT = system.getProperty(RemoteJVMFactory.RMI_PORT_PROPERTY);
      String OUTPUT_DIR = system.getProperty(RemoteJVMFactory.OUTPUT_DIR);
      int id = system.getInteger(RemoteJVMFactory.JVM_ID);

      if (RMI_HOST == null || RMI_PORT == null || OUTPUT_DIR == null) {
        throw new IllegalStateException(
            "ChildJVM must be launched with all required system properties set.");
      }

      File outputDir = new File(OUTPUT_DIR);
      // Clean up the output directory before the test runs
      FileUtils.deleteQuietly(outputDir);
      outputDir.mkdirs();
      PrintStream out = new PrintStream(new File(outputDir, "system.log"));
      system.setOut(out);
      system.setErr(out);

      ControllerRemote controller = (ControllerRemote) rmi
          .lookup("//" + RMI_HOST + ":" + RMI_PORT + "/" + RemoteJVMFactory.CONTROLLER);

      SharedContext sharedContext = controller.getsharedContext();
      DefaultTestContext context = new DefaultTestContext(sharedContext, outputDir, id);

      Worker worker = new Worker(context);

      controller.addWorker(id, worker);

      // Wait until the controller shuts down
      // If the controller shuts down, this will throw an exception
      try {
        while (controller.ping()) {
          Thread.sleep(pingTime);
        }
      } catch (RemoteException e) {
        // If we get a RemoteException, the controller has shut down
        // exit gracefully
      }

      system.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      // Force a system exit. Because we created an RMI object, an exception from the main
      // thread would not otherwise cause this process to exit
      system.exit(1);
    }
  }

}
