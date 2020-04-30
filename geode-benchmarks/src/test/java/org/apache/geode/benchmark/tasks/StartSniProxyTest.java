package org.apache.geode.benchmark.tasks;

import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StartSniProxyTest {

  @Test
  public void generateConfigTest() {
    final StartSniProxy starter = new StartSniProxy(42);
    final String config =
        starter.generateHaProxyConfig(Collections.singleton("one"), Collections.singleton("two"));
    assertThat(config).isEqualTo("defaults\n"
        + "  timeout client 1000\n"
        + "  timeout connect 1000\n"
        + "  timeout server 1000\n"
        + "frontend sniproxy\n"
        + "  bind *:15443\n"
        + "  mode tcp\n"
        + "  tcp-request inspect-delay 5s\n"
        + "  tcp-request content accept if { req_ssl_hello_type 1 }\n"
        + "  log stdout format raw  local0  debug\n"
        + "  use_backend locators-one if { req.ssl_sni -i one }\n"
        + "  use_backend servers-two if { req.ssl_sni -i two }\n"
        + "  default_backend locators-one\n"
        + "backend locators-one\n"
        + "  mode tcp\n"
        + "  server locator1 one:42\n"
        + "backend servers-two\n"
        + "  mode tcp\n"
        + "  server server1 two:40404\n");
  }
}