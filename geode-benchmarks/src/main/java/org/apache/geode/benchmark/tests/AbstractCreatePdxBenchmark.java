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
package org.apache.geode.benchmark.tests;

import org.apache.geode.benchmark.topology.ClientServerTopology;
import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.TestConfig;

import java.util.Properties;

import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.LOCATOR;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.SERVER;
import static org.apache.geode.distributed.ConfigurationProperties.LOG_LEVEL;

public class AbstractCreatePdxBenchmark implements PerformanceTest {
    @Override
    public TestConfig configure() {
        TestConfig config = GeodeBenchmark.createConfig();
        config.threads(Runtime.getRuntime().availableProcessors() * 2);
        Properties customProps = new Properties();
        // The PdxType creation process generates a large amount of info-level log output which creates
        // unwanted overhead, so the log level is set to "WARN" to avoid this
        customProps.setProperty(LOG_LEVEL, "WARN");
        config.props(SERVER, customProps);
        config.props(LOCATOR, customProps);
        ClientServerTopology.configure(config);
        return config;
    }
}
