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
package org.apache.geode.infrastructure.aws;

import static java.lang.System.getProperty;
import static software.amazon.awssdk.services.ec2.model.InstanceType.C5_18_XLARGE;

import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.PlacementStrategy;
import software.amazon.awssdk.services.ec2.model.Tenancy;

import org.apache.geode.infrastructure.BenchmarkMetadata;

/**
 * Static methods to generate common strings used for AWS infrastructure.
 */
class AwsBenchmarkMetadata extends BenchmarkMetadata {
  public static final String USER = "geode";
  public static final int POLL_INTERVAL = 15000;
  public static InstanceType INSTANCE_TYPE = InstanceType.fromValue(
      getProperty("INSTANCE_TYPE", C5_18_XLARGE.toString()));
  public static Tenancy TENANCY = Tenancy.DEDICATED;

  public static String securityGroup(String tag) {
    return BenchmarkMetadata.benchmarkString(tag, "securityGroup");
  }

  public static String placementGroup(String tag) {
    return BenchmarkMetadata.benchmarkString(tag, "placementGroup");
  }

  public static String launchTemplate(String tag) {
    return BenchmarkMetadata.benchmarkString(tag, "launchTemplate");
  }

  public static String keyPair(String tag) {
    return BenchmarkMetadata.benchmarkString(tag, "keyPair");
  }

  public static String keyPairFileName(String tag) {
    return BenchmarkMetadata.benchmarkPrivateKeyFileName(tag);
  }

  public static String metadataFileName(String tag) {
    return BenchmarkMetadata.benchmarkMetadataFileName(tag);
  }

  public static InstanceType instanceType() {
    return INSTANCE_TYPE;
  }

  public static Tenancy tenancy() {
    return TENANCY;
  }

  public static PlacementStrategy placementGroupStrategy() {
    return PlacementStrategy.CLUSTER;
  }
}
