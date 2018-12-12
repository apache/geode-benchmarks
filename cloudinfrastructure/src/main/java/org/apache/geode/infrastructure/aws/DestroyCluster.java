package org.apache.geode.infrastructure.aws;


import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;


public class DestroyCluster {
  static Ec2Client ec2 = Ec2Client.create();

  public static void main(String[] args) throws IOException {
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
    }

  }

  private static void deletePlacementGroup(String benchmarkTag) {

    try {
      ec2.deletePlacementGroup(DeletePlacementGroupRequest.builder()
          .groupName(AwsBenchmarkMetadata.placementGroup(benchmarkTag))
          .build());

      System.out.println("Placement Group for cluster '" + benchmarkTag + "' deleted.");
    } catch(Ec2Exception e) {
      System.out.println("We got an exception while deleting the placement group");

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
    }
  }
}
