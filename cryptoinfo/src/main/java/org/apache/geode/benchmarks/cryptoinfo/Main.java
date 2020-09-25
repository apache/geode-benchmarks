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

import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public class Main {
  public static void main(String[] args) throws NoSuchAlgorithmException {
    SSLContext context = SSLContext.getDefault();
    final PrintStream out = System.out;
    out.println("Provider: ");
    printProvider(out, context.getProvider());
    out.println("SSL Parameters: ");
    printSSLParameters(out, context.getSupportedSSLParameters());
  }

  private static void printSSLParameters(final PrintStream out, final SSLParameters sslParameters) {
    out.println("Protocols:");
    printStrings(out, sslParameters.getProtocols());
    out.println("Cipher Suites:");
    printStrings(out, sslParameters.getCipherSuites());
  }

  private static void printStrings(PrintStream out, String[] strings) {
    for (String string : strings) {
      out.println(string);
    }
  }

  private static void printProvider(final PrintStream out, final Provider provider) {
    out.print("Name: ");
    out.println(provider.getName());
    out.print("Info: ");
    out.println(provider.getInfo());
    out.print("Version: ");
    out.println(provider.getVersion());
  }

}
