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
