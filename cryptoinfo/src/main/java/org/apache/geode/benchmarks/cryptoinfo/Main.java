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

package org.apache.geode.benchmarks.cryptoinfo;

import static java.lang.System.getProperty;

import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

public class Main {
  public static void main(final String[] args) throws NoSuchAlgorithmException {
    final SSLContext context = SSLContext.getDefault();
    final PrintStream out = System.out;
    indentedPrintLine(out, 0, "Java:");
    printJavaInfo(out, 1);
    indentedPrintLine(out, 0, "Provider: ");
    printProvider(out, 1, context.getProvider());
    indentedPrintLine(out, 0, "SSL Parameters: ");
    printSSLParameters(out, 1,  context.getSupportedSSLParameters());
    indentedPrintLine(out, 0, "SSL Engine: ");
    printSSLEngine(out, 1, context.createSSLEngine());
  }

  private static void indentedPrintLine(final PrintStream out, final int depth, final String x) {
    for (int i = 0; i < depth; ++i) {
      out.print("  ");
    }
    out.println(x);
  }

  private static void printSSLEngine(final PrintStream out, final int depth, final SSLEngine sslEngine) {
    indentedPrintLine(out, depth, "Enabled Protocols: ");
    printStrings(out, depth + 1, sslEngine.getEnabledProtocols());
    indentedPrintLine(out, depth, "Enabled Cipher Suites: ");
    printStrings(out, depth + 1, sslEngine.getEnabledCipherSuites());
  }

  private static void printJavaInfo(final PrintStream out, final int depth) {
    indentedPrintLine(out, depth, getProperty("java.home"));
    indentedPrintLine(out,depth , getProperty("java.vendor"));
    indentedPrintLine(out,depth , getProperty("java.version"));
  }

  private static void printSSLParameters(final PrintStream out, final int depth, final SSLParameters sslParameters) {
    indentedPrintLine(out, depth, "Protocols:");
    printStrings(out, depth + 1,  sslParameters.getProtocols());
    indentedPrintLine(out, depth, "Cipher Suites:");
    printStrings(out, depth+1, sslParameters.getCipherSuites());
  }

  private static void printStrings(PrintStream out, final int depth, String[] strings) {
    for (String string : strings) {
      indentedPrintLine(out, depth, string);
    }
  }

  private static void printProvider(final PrintStream out, final int depth, final Provider provider) {
    indentedPrintLine(out, depth, "Name: " + provider.getName());
    indentedPrintLine(out, depth, "Info: " + provider.getInfo());
    indentedPrintLine(out, depth, "Version: " + provider.getVersion());
  }

}
