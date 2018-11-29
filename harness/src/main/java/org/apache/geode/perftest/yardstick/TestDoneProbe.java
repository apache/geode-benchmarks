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

package org.apache.geode.perftest.yardstick;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriver;
import org.yardstickframework.BenchmarkProbe;
import org.yardstickframework.BenchmarkProbePoint;

class TestDoneProbe implements BenchmarkProbe {

  CountDownLatch done = new CountDownLatch(1);

  public void await() throws InterruptedException {
    done.await();
  }

  @Override
  public void start(BenchmarkDriver drv, BenchmarkConfiguration cfg) throws Exception {

  }

  @Override
  public void stop() throws Exception {
    done.countDown();
  }

  @Override
  public Collection<String> metaInfo() {
    return null;
  }

  @Override
  public Collection<BenchmarkProbePoint> points() {
    return Collections.emptyList();
  }

  @Override
  public void buildPoint(long time) {

  }
}
