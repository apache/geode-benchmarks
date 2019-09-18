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

import static java.lang.Thread.sleep;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DeleteLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.DeletePlacementGroupRequest;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DescribeHostsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeHostsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Host;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.ReleaseHostsRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

import org.apache.geode.infrastructure.BenchmarkMetadata;

public class DestroyCluster {
  private static Ec2Client ec2 = Ec2Client.create();

  public static void main(String[] args) throws InterruptedException {
    if (args.length != 1) {
      throw new IllegalStateException("This takes one argument, the cluster tag to work with.");
    }
    String benchmarkTag = args[0];

    if (benchmarkTag == null || benchmarkTag.isEmpty()) {
      throw new IllegalStateException("No valid tag found.");
    }

    deleteInstances(benchmarkTag);
    releaseHosts(benchmarkTag);
    deleteLaunchTemplate(benchmarkTag);
    deleteSecurityGroup(benchmarkTag);
    deletePlacementGroup(benchmarkTag);
    deleteKeyPair(benchmarkTag);
    deleteMetadata(benchmarkTag);
  }

  private static void releaseHosts(String benchmarkTag) {
    DescribeHostsResponse hosts = ec2.describeHosts(DescribeHostsRequest.builder()
        .filter(Filter.builder()
            .name("tag:" + BenchmarkMetadata.PREFIX)
            .values(benchmarkTag)
            .build())
        .build());

    List<String> hostIds = hosts.hosts().stream().map(Host::hostId).collect(Collectors.toList());

    ec2.releaseHosts(ReleaseHostsRequest.builder().hostIds(hostIds).build());

    System.out.println("Hosts for cluster '" + benchmarkTag + "' released.");
  }

  private static void deleteKeyPair(String benchmarkTag) {
    try {
      System.out.println("Deleting cluster keypair: " + AwsBenchmarkMetadata.keyPair(benchmarkTag));
      ec2.deleteKeyPair(DeleteKeyPairRequest.builder()
          .keyName(AwsBenchmarkMetadata.keyPair(benchmarkTag))
          .build());
      Files.deleteIfExists(Paths.get(AwsBenchmarkMetadata.keyPairFileName(benchmarkTag)));
      System.out.println("Key Pair for cluster '" + benchmarkTag + "' deleted.");
    } catch (Exception e) {
      System.out.println("We got an exception while deleting the Key pair");
      System.out.println("Exception message: " + e);
    }
  }

  private static void deleteMetadata(String benchmarkTag) {
    try {
      Files.deleteIfExists(Paths.get(AwsBenchmarkMetadata.metadataFileName(benchmarkTag)));
      System.out.println("Metadata for cluster '" + benchmarkTag + "' deleted.");
    } catch (Exception e) {
      System.out.println("We got an exception while deleting the Key pair");
      System.out.println("Exception message: " + e);
    }
  }

  private static void deleteInstances(String benchmarkTag) throws InterruptedException {
    // delete instances
    try {
      DescribeInstancesResponse dir = ec2.describeInstances(DescribeInstancesRequest.builder()
          .filters(Filter.builder()
              .name("tag:" + BenchmarkMetadata.PREFIX).values(benchmarkTag).build())
          .build());
      Stream<Instance> instanceStream = dir.reservations()
          .stream()
          .flatMap(reservation -> reservation.instances().stream());

      List<String> instanceIds = dir
          .reservations()
          .stream()
          .flatMap(reservation -> reservation
              .instances()
              .stream())
          .map(Instance::instanceId)
          .collect(Collectors.toList());

      ec2.terminateInstances(TerminateInstancesRequest.builder()
          .instanceIds(instanceIds)
          .build());
      System.out.println("Waiting for cluster instances to terminate.");
      while (ec2.describeInstances(DescribeInstancesRequest.builder()
          .instanceIds(instanceIds)
          .filters(Filter.builder()
              .name("instance-state-name")
              .values("pending", "running", "shutting-down", "stopping", "stopped")
              .build())
          .build()).reservations().stream().flatMap(reservation -> reservation.instances().stream())
          .count() > 0) {
        sleep(AwsBenchmarkMetadata.POLL_INTERVAL);
        System.out.println("Continuing to wait.");
      }

      System.out.println("Instances for cluster '" + benchmarkTag + "' deleted.");
    } catch (Exception e) {
      System.out.println("We got an exception while deleting the instances");
      System.out.println("Exception message: " + e);
    }
  }

  private static void deletePlacementGroup(String benchmarkTag) {

    try {
      ec2.deletePlacementGroup(DeletePlacementGroupRequest.builder()
          .groupName(AwsBenchmarkMetadata.placementGroup(benchmarkTag))
          .build());

      System.out.println("Placement Group for cluster '" + benchmarkTag + "' deleted.");
    } catch (Exception e) {
      System.out.println("We got an exception while deleting the placement group");
      System.out.println("Exception message: " + e);
    }
  }

  private static void deleteSecurityGroup(String benchmarkTag) {

    try {
      ec2.deleteSecurityGroup(DeleteSecurityGroupRequest.builder()
          .groupName(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
          .build());

      System.out.println("Security Group for cluster '" + benchmarkTag + "' deleted.");
    } catch (Exception e) {
      System.out.println("We got an exception while deleting the security group");
      System.out.println("Exception message: " + e);
    }
  }

  private static void deleteLaunchTemplate(String benchmarkTag) {

    try {
      ec2.deleteLaunchTemplate(DeleteLaunchTemplateRequest.builder()
          .launchTemplateName(AwsBenchmarkMetadata.launchTemplate(benchmarkTag))
          .build());

      System.out.println("Launch template for cluster '" + benchmarkTag + "' deleted.");
    } catch (Exception e) {
      System.out.println("We got an exception while deleting the launch template");
      System.out.println("Exception message: " + e);
    }
  }
}
