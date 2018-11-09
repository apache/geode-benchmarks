package org.apache.geode.perftest.jvms;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.perftest.infrastructure.local.LocalInfrastructure;

public class JVMManagerTest {
  @Rule
  public final TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void canExecuteCodeOnWorker() throws Exception {
    JVMManager jvmManager = new JVMManager();
    Map<String, Integer> roles = Collections.singletonMap("worker", 1);
    RemoteJVMs jvms = jvmManager.launch(new LocalInfrastructure(1), roles);


    File tempFile = new File(temporaryFolder.newFolder(), "tmpfile").getAbsoluteFile();
    jvms.execute(context -> {
     tempFile.createNewFile();
    },"worker");

    assertTrue(tempFile.exists());
  }

}