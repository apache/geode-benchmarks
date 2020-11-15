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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class YardstickPercentileSensorParserTest {

  @TempDir
  Path temporaryFolder;

  @Test
  public void parsesInputFile() throws IOException {
    final File testFolder = temporaryFolder.resolve("testFolder").toFile();
    assertTrue(testFolder.mkdirs());
    generateSimpleTestFile(testFolder);

    YardstickPercentileSensorParser parser = new YardstickPercentileSensorParser();
    parser.parseResults(testFolder);
  }

  @Test
  public void parsesInputFileAndFindsAlignedPercentiles() throws IOException {
    final File testFolder = temporaryFolder.resolve("testFolder").toFile();
    assertTrue(testFolder.mkdirs());
    generateSimpleTestFile(testFolder);

    YardstickPercentileSensorParser parser = new YardstickPercentileSensorParser();
    parser.parseResults(testFolder);
    assertEquals(400, parser.getPercentile(30), 0.01);
    assertEquals(800, parser.getPercentile(95), 0.01);
    assertEquals(900, parser.getPercentile(100), 0.01);
  }

  @Test
  public void parsesInputFileAndFindsUnalignedPercentiles() throws IOException {
    final File testFolder = temporaryFolder.resolve("testFolder").toFile();
    assertTrue(testFolder.mkdirs());
    generateSimpleTestFile(testFolder);

    YardstickPercentileSensorParser parser = new YardstickPercentileSensorParser();
    parser.parseResults(testFolder);
    assertEquals(450, parser.getPercentile(40), 0.01);
    assertEquals(633.333, parser.getPercentile(75), 0.01);
    assertEquals(720, parser.getPercentile(87), 0.01);
  }

  private void generateSimpleTestFile(File testFolder) throws IOException {
    final File testFile = new File(testFolder, YardstickPercentileSensorParser.sensorOutputFile);
    BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
    output.write("0,0.00");
    output.newLine();
    output.write("100,0.05");
    output.newLine();
    output.write("200,0.10");
    output.newLine();
    output.write("300,0.15");
    output.newLine();
    output.write("400,0.20");
    output.newLine();
    output.write("500,0.20");
    output.newLine();
    output.write("600,0.15");
    output.newLine();
    output.write("700,0.10");
    output.newLine();
    output.write("800,0.05");
    output.newLine();
    output.write("900,0.00");
    output.newLine();
    output.close();
  }

  @Test
  public void percentileSearchNormalizesInput() throws IOException {
    final File testFolder = temporaryFolder.resolve("testFolder").toFile();
    assertTrue(testFolder.mkdirs());
    final File testFile = new File(testFolder, YardstickPercentileSensorParser.sensorOutputFile);
    assertTrue(testFile.createNewFile());
    BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
    output.write("0,0.00");
    output.newLine();
    output.write("100,0.05");
    output.newLine();
    output.write("200,0.10");
    output.newLine();
    output.write("300,0.15");
    output.newLine();
    output.write("400,0.20");
    output.close();

    YardstickPercentileSensorParser parser = new YardstickPercentileSensorParser();
    parser.parseResults(testFolder);
    assertEquals(500, parser.getPercentile(100), 0.001);
    assertEquals(475, parser.getPercentile(90), 0.001);
  }

  @Test
  public void throwsExceptionOnMissingFile() throws IOException {
    final File testFolder = temporaryFolder.resolve("testFolder").toFile();
    assertTrue(testFolder.mkdirs());
    YardstickPercentileSensorParser parser = new YardstickPercentileSensorParser();
    Assertions.assertThrows(IOException.class, () -> parser.parseResults(testFolder));
  }

  @Test
  public void throwsExceptionOnBadInput() throws IOException {
    final File testFolder = temporaryFolder.resolve("testFolder").toFile();
    assertTrue(testFolder.mkdirs());
    final File testFile = new File(testFolder, YardstickPercentileSensorParser.sensorOutputFile);
    assertTrue(testFile.createNewFile());
    BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
    output.write("0,0.00");
    output.newLine();
    output.write("100,0.05");
    output.newLine();
    output.write("200,0.10");
    output.newLine();
    output.write("300,duck");
    output.newLine();
    output.write("400,0.20");
    output.newLine();
    output.write("500,0.20");
    output.close();

    YardstickPercentileSensorParser parser = new YardstickPercentileSensorParser();
    assertThrows(IOException.class, () -> parser.parseResults(testFolder));
  }

  @Test
  public void ignoreYardstickCommentAndMetadataLines() throws IOException {
    final File testFolder = temporaryFolder.resolve("testFolder").toFile();
    assertTrue(testFolder.mkdirs());
    final File testFile = new File(testFolder, YardstickPercentileSensorParser.sensorOutputFile);
    assertTrue(testFile.createNewFile());
    BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
    output.write("0,0.40");
    output.newLine();
    output.write("--This is a comment");
    output.newLine();
    output.write("@@Classname");
    output.newLine();
    output.write("**Metadata");
    output.newLine();
    output.write("100,0.20");
    output.newLine();
    output.write("200,0.40");
    output.newLine();
    output.close();

    YardstickPercentileSensorParser parser = new YardstickPercentileSensorParser();
    parser.parseResults(testFolder);
    assertEquals(100, parser.getPercentile(40), 0.01);
  }

}
