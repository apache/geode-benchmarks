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

package org.apache.geode.benchmark.tasks.redis;


import static org.apache.geode.redis.internal.RegionProvider.REDIS_DATA_REGION;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestContext;

public class InitGedisRegion implements Task {

  @Override
  public void run(final TestContext context) throws Exception {
    final Cache cache = (Cache) context.getAttribute("SERVER_CACHE");
    final Region<?, ?> region = cache.getRegion(REDIS_DATA_REGION);
    PartitionRegionHelper.assignBucketsToPartitions(region);
  }
}
