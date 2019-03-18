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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestRunner;
import org.apache.geode.perftest.infrastructure.InfrastructureFactory;
import org.apache.geode.perftest.jvms.RemoteJVMFactory;
import org.apache.geode.perftest.jvms.RemoteJVMs;

/**
 * Runner that executes a {@link PerformanceTest}, using
 * a provided {@link InfrastructureFactory}.
 *
 * This is the main entry point for running tests. Users should
 * implement {@link PerformanceTest} to define there tests in
 * a declarative fashion and then execute them this runner.
 */
public class DefaultTestRunner implements TestRunner {
  private static final Logger logger = LoggerFactory.getLogger(DefaultTestRunner.class);


  private final RemoteJVMFactory remoteJvmFactory;
  private File outputDir;

  public DefaultTestRunner(RemoteJVMFactory remoteJvmFactory, File outputDir) {
    this.remoteJvmFactory = remoteJvmFactory;
    this.outputDir = outputDir;
  }

  @Override
  public void runTest(PerformanceTest test) throws Exception {
    TestConfig config = test.configure();
    String testName = test.getClass().getName();
    runTest(config, testName);
  }

  protected void runTest(TestConfig config, String testName)
      throws Exception {
    int nodes = config.getTotalJVMs();
    File benchmarkOutput = new File(outputDir, testName);
    if (benchmarkOutput.exists()) {
      throw new IllegalStateException(
          "Benchmark output directory already exists: " + benchmarkOutput.getPath());
    }

    benchmarkOutput.mkdirs();
    String metadataFilename = outputDir + "/metadata.json";
    Path metadataOutput = Paths.get(metadataFilename);
    JSONObject JSONmetadata = new JSONObject();

    if (metadataOutput.toFile().exists()) {
      JSONmetadata = new JSONObject(new String(Files.readAllBytes(metadataOutput)));
    } else {
      String metadata = System.getProperty("TEST_METADATA");
      if (!(metadata == null) && !metadata.isEmpty()) {
        JSONObject testMetadata = new JSONObject();
        String[] metadataEntries = metadata.split(",");
        for (String data : metadataEntries) {
          String[] kv = data.split(":");
          if (kv.length == 2) {
            testMetadata.put(kv[0], kv[1]);
          }
        }
        addVersionProperties(testMetadata, getVersionProperties());
        JSONmetadata.put("testMetadata", testMetadata);
      }
    }

    try {
      JSONArray testNames = JSONmetadata.getJSONArray("testNames");
      testNames.put(testName);
      JSONmetadata.put("testNames", testNames);
    } catch (org.json.JSONException e) {
      JSONArray testNames = new JSONArray();
      testNames.put(testName);
      JSONmetadata.put("testNames", testNames);
    }

    FileWriter metadataWriter = new FileWriter(metadataOutput.toFile().getAbsoluteFile());
    metadataWriter.write(JSONmetadata.toString());
    metadataWriter.flush();

    Map<String, Integer> roles = config.getRoles();
    Map<String, List<String>> jvmArgs = config.getJvmArgs();

    logger.info("Lauching JVMs...");
    // launch JVMs in parallel, hook them up
    RemoteJVMs remoteJVMs = remoteJvmFactory.launch(roles, jvmArgs);
    try {
      logger.info("Starting before tasks...");
      runTasks(config.getBefore(), remoteJVMs);

      logger.info("Starting workload tasks...");
      runTasks(config.getWorkload(), remoteJVMs);

      logger.info("Starting after tasks...");
      runTasks(config.getAfter(), remoteJVMs);
    } finally {
      // Close before copy otherwise logs, stats, and profiles are incomplete or missing.
      remoteJVMs.closeController();

      logger.info("Copying results...");
      remoteJVMs.copyResults(benchmarkOutput);

      remoteJVMs.closeInfra();
    }

  }

  private void addVersionProperties(JSONObject jsonMetadata, Properties versionProperties) {
    jsonMetadata.put("source_version", versionProperties.getProperty("Product-Version"));
    jsonMetadata.put("source_branch", versionProperties.getProperty("Source-Repository"));
    jsonMetadata.put("source_revision", versionProperties.getProperty("Source-Revision"));
  }

  private Properties getVersionProperties() throws IOException {
    Properties versionProperties = new Properties();
    InputStream in = ClassLoader.getSystemResourceAsStream("org/apache/geode/internal/GemFireVersion.properties");
    versionProperties.load(in);
    return versionProperties;
  }

  private void runTasks(List<TestConfig.TestStep> steps,
      RemoteJVMs remoteJVMs) {
    steps.forEach(testStep -> {
      remoteJVMs.execute(testStep.getTask(), testStep.getRoles());
    });
  }

  public RemoteJVMFactory getRemoteJvmFactory() {
    return remoteJvmFactory;
  }
}
