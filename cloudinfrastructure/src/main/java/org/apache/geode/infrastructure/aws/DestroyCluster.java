package org.apache.geode.infrastructure.aws;


import static java.lang.Thread.sleep;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.DeleteLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.DeletePlacementGroupRequest;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

import org.apache.geode.infrastructure.BenchmarkMetadata;


public class DestroyCluster {
  private static Ec2Client ec2 = Ec2Client.create();

  public static void main(String[] args) throws IOException, InterruptedException {
    boolean valid = true;
    if (args.length != 1) {
      System.out.println("This takes one argument, the cluster tag to work with.");
      System.exit(1);
      return;
    }
    String benchmarkTag = args[0];

    if (benchmarkTag == null || benchmarkTag.isEmpty()) {
      valid = false;
    }

    if (!valid) {
      System.out.println("No valid tag found.");
      System.exit(1);
      return;
    }
    deleteInstances(benchmarkTag);
    deleteLaunchTemplate(benchmarkTag);
    deleteSecurityGroup(benchmarkTag);
    deletePlacementGroup(benchmarkTag);

    try {
      DeleteKeyPairResponse dkpr = ec2.deleteKeyPair(DeleteKeyPairRequest.builder().keyName(AwsBenchmarkMetadata.keyPair(benchmarkTag)).build());
      Files.deleteIfExists(Paths.get(AwsBenchmarkMetadata.keyPairFileName(benchmarkTag)));
    } catch(Ec2Exception e) {
      // we don't care
    }
    catch(NoSuchFileException e)
    {
    }
    catch(DirectoryNotEmptyException e)
    {
      System.out.println("Directory is not empty.");
    }
    catch(IOException e)
    {
      System.out.println("Unable to delete key pair file for cluster '" + benchmarkTag + "'.");
      System.out.println("Exception message: " + e.getMessage());
    }
    System.out.println("Key Pair for cluster'" + benchmarkTag + "' deleted.");
  }

  private static void deleteInstances(String benchmarkTag) throws InterruptedException {
    // delete instances
    try {
      DescribeInstancesResponse dir = ec2.describeInstances(DescribeInstancesRequest.builder()
          .filters(Filter.builder().name("tag:" + BenchmarkMetadata.PREFIX).values(benchmarkTag).build())
          .build());
      Stream<Instance> instanceStream = dir.reservations()
          .stream()
          .flatMap(reservation -> reservation.instances().stream());

      List<String>
          instanceIds = dir
          .reservations()
          .stream()
          .flatMap(reservation -> reservation
              .instances()
              .stream())
          .map(Instance::instanceId)
          .collect(Collectors.toList());

//      dir.reservations().stream().findFirst().instances().stream().map(reservation -> reservation.)
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
          .build()).reservations().stream().flatMap(reservation -> reservation.instances().stream()).count() > 0) {
         sleep(60000);
         System.out.println("Continuing to wait.");
      }

    } catch(Ec2Exception e) {
      System.out.println("We got an exception while deleting the instances");
      System.out.println("Exception message: " + e.getMessage());

      // we don't care what happens.

    }
    //
    System.out.println("Instances for cluster '" + benchmarkTag + "' deleted.");
  }

  private static void deletePlacementGroup(String benchmarkTag) {

    try {
      ec2.deletePlacementGroup(DeletePlacementGroupRequest.builder()
          .groupName(AwsBenchmarkMetadata.placementGroup(benchmarkTag))
          .build());

      System.out.println("Placement Group for cluster '" + benchmarkTag + "' deleted.");
    } catch(Ec2Exception e) {
      System.out.println("We got an exception while deleting the placement group");
      System.out.println("Exception message: " + e.getMessage());

      // we don't care what happens.
    }
  }

  private static void deleteSecurityGroup(String benchmarkTag) {

    try {
      ec2.deleteSecurityGroup(DeleteSecurityGroupRequest.builder()
          .groupName(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
          .build());

      System.out.println("Security Group for cluster '" + benchmarkTag + "' deleted.");
    } catch(Ec2Exception e) {
      System.out.println("We got an exception while deleting the security group");
      System.out.println("Exception message: " + e.getMessage());

      // we don't care what happens.
    }
  }

  private static void deleteLaunchTemplate(String benchmarkTag) {

    try {
      ec2.deleteLaunchTemplate(DeleteLaunchTemplateRequest.builder()
          .launchTemplateName(AwsBenchmarkMetadata.launchTemplate(benchmarkTag))
          .build());

      System.out.println("Launch template for cluster '" + benchmarkTag + "' deleted.");
    } catch(Ec2Exception e) {
      System.out.println("We got an exception while deleting the launch template");
      System.out.println("Exception message: " + e.getMessage());
    }
  }
}
