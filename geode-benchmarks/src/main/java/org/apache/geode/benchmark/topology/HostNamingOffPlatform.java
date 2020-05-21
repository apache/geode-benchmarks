package org.apache.geode.benchmark.topology;

import java.net.InetAddress;

public class HostNamingOffPlatform {
  public final String externalName;
  public final InetAddress internalAddy;

  public HostNamingOffPlatform(final String externalName, final InetAddress internalAddy) {
    this.externalName = externalName;
    this.internalAddy = internalAddy;
  }
}
