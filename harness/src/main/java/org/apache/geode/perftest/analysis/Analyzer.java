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
package org.apache.geode.perftest.analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.geode.perftest.yardstick.analysis.YardstickHdrHistogramParser;
import org.apache.geode.perftest.yardstick.analysis.YardstickPercentileSensorParser;
import org.apache.geode.perftest.yardstick.analysis.YardstickThroughputSensorParser;

public class Analyzer {

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println(
          "Analyzer takes two test output directories as arguments and a flag to indicate if running in CI. Order matters: test results followed by baseline run result, followed by CI flag.");
      System.exit(1);
      return;
    }

    String baselineResultArg = args[0];
    String testResultArg = args[1];
    String isCIArg = args[2];

    File testResultDir = new File(testResultArg);
    File baselineResultDir = new File(baselineResultArg);

    boolean valid = true;
    if (!testResultDir.exists()) {
      System.out.println("Unable to find test result directory: " + testResultArg);
      valid = false;
    }
    if (!baselineResultDir.exists()) {
      System.out.println("Unable to find test result directory: " + baselineResultArg);
      valid = false;
    }
    if (!valid) {
      System.exit(1);
      return;
    }

    boolean isCI = isCIArg.equals("1");

    System.out.println("Running analyzer");
    System.out.println(
        "Comparing test result at " + testResultArg + " to baseline at " + baselineResultArg);

    BenchmarkRunAnalyzer analyzer = new BenchmarkRunAnalyzer();
    analyzer.addProbe(new YardstickThroughputSensorParser());
    analyzer.addProbe(new YardstickPercentileSensorParser());
    analyzer.addProbe(new YardstickHdrHistogramParser());

    BenchmarkRunResult benchmarkRunResult =
        analyzer.analyzeTestRun(baselineResultDir, testResultDir);
    benchmarkRunResult.writeResult(new PrintWriter(System.out));
    /* throw exc if failed? */

    boolean isSignificantlyBetter = false;
    boolean isHighWaterCandidate = true;
    StringBuilder message = new StringBuilder();
    for (BenchmarkRunResult.BenchmarkResult benchmarkResult : benchmarkRunResult
        .getBenchmarkResults()) {
      for (BenchmarkRunResult.ProbeResult probeResult : benchmarkResult.probeResults) {
        if (probeResult.description.equals("average latency")) {
          if (probeResult.getDifference() > 0) {
            isHighWaterCandidate = false;
            if (probeResult.getDifference() >= 0.05) {
              message.append("BENCHMARK FAILED: ").append(benchmarkResult.name)
                  .append(" average latency is 5% worse than baseline.\n");
            }
          } else if(probeResult.getDifference() <= -0.5) {
            isSignificantlyBetter = true;
          }
        }
      }
    }

    if (isCI && isHighWaterCandidate && isSignificantlyBetter) {
      // this commit is the new high water mark, send an email
      Session session = Session.getDefaultInstance(new Properties(), null);
      MimeMessage emailContent = new MimeMessage(session);
      try {
        emailContent.setFrom(new InternetAddress("hbales@pivotal.io"));
        emailContent.addRecipient(Message.RecipientType.TO, new InternetAddress("hbales@pivotal.io"));
        emailContent.setHeader("emailHeader","New high watermark");
        emailContent.setText("Hello world");
      } catch (MessagingException e) {
        e.printStackTrace();
      }


    }

    if (message.length() > 0) {
      System.out.println(message);
      System.exit(1);
    }

  }
}
