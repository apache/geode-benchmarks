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

package org.apache.geode.perftest.yardstick.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class YardstickThroughputSensorParserTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void parsesInputFile() throws IOException {
    final File testFolder = temporaryFolder.newFolder("testFolder");
    final File testFile = new File(testFolder, YardstickThroughputSensorParser.sensorOutputFile);
    BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
    output.write("1542151468,42906.00,371126.31");
    output.newLine();
    output.write("1542151469,45134.00,353812.31");
    output.newLine();
    output.write("1542151470,47091.00,339720.36");
    output.newLine();
    output.write("1542151471,47567.00,335120.03");
    output.newLine();
    output.close();

    YardstickThroughputSensorParser parser = new YardstickThroughputSensorParser();
    parser.parseResults(testFolder);
    Assert.assertEquals(45674.5, parser.getAverageThroughput(), .01);
  }

  @Test(expected = IOException.class)
  public void throwsExceptionOnMissingFile() throws IOException {
    final File testFolder = temporaryFolder.newFolder("testFolder");
    YardstickThroughputSensorParser parser = new YardstickThroughputSensorParser();
    parser.parseResults(testFolder);
  }

  @Test(expected = IOException.class)
  public void throwsExceptionOnBadInput() throws IOException {
    final File testFolder = temporaryFolder.newFolder("testFolder");
    final File testFile = new File(testFolder, YardstickThroughputSensorParser.sensorOutputFile);
    BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
    output.write("1542151468,42906.00,371126.31");
    output.newLine();
    output.write("1542151469,45134.00,353812.31");
    output.newLine();
    output.write("This is not a proper comment");
    output.newLine();
    output.write("1542151471,47567.00,335120.03");
    output.newLine();
    output.close();

    YardstickThroughputSensorParser parser = new YardstickThroughputSensorParser();
    parser.parseResults(testFolder);
  }

  @Test
  public void ignoreYardstickCommentAndMetadataLines() throws IOException {
    final File testFolder = temporaryFolder.newFolder("testFolder");
    final File testFile = new File(testFolder, YardstickThroughputSensorParser.sensorOutputFile);
    BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
    output.write("--This is a comment");
    output.newLine();
    output.write("@@Classname");
    output.newLine();
    output.write("**Metadata");
    output.newLine();
    output.write("1542151468,42906.00,371126.31");
    output.newLine();
    output.close();

    YardstickThroughputSensorParser parser = new YardstickThroughputSensorParser();
    parser.parseResults(testFolder);
    Assert.assertEquals(42906f, parser.getAverageThroughput(), .01);
  }
}
