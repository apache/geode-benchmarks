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

package org.apache.geode.benchmark.tasks;

import static java.lang.String.valueOf;

import org.apache.geode.benchmark.LongRange;
import org.apache.geode.benchmark.topology.Roles;

public class PrePopulateRegionString extends AbstractPrePopulateRegion<String, String> {

  public PrePopulateRegionString() {}

  public PrePopulateRegionString(LongRange keyRangeToPrepopulate) {
    super(keyRangeToPrepopulate);
  }

  public PrePopulateRegionString(LongRange keyRangeToPrepopulate,
      Roles targetRole) {
    super(keyRangeToPrepopulate, targetRole);
  }

  @Override
  protected String getKey(final long i) {
    return valueOf(i);
  }

  @Override
  protected String getValue(final long i) {
    return valueOf(i);
  }

}
