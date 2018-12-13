package org.apache.geode.infrastructure.aws;


import org.apache.geode.infrastructure.BenchmarkMetadata;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class LaunchCluster {
  static Ec2Client ec2 = Ec2Client.create();

  public static void main(String[] args) throws IOException {
    boolean valid = true;
    if (args.length != 1) {
      System.exit(1);
      return;
    }
    String benchmarkTag = args[0];

    if (benchmarkTag == null || benchmarkTag.isEmpty()) {
      valid = false;
    }

    if (!valid) {
      System.exit(1);
      return;
    }

    createKeyPair(benchmarkTag);

    Image newestImage = getNewestImage();

    List<Tag> tags = getTags(benchmarkTag);

    if (!createPlacementGroup(benchmarkTag)) {
      System.exit(1);
    }

    if (!createSecurityGroup(benchmarkTag, tags)) {
      System.exit(1);
    }



    if (!createLaunchTemplate(benchmarkTag, newestImage)) {
      System.exit(1);
    }

    // launch instances

    try {
      ec2.runInstances(RunInstancesRequest.builder()
          .launchTemplate(LaunchTemplateSpecification.builder()
              .launchTemplateName(AwsBenchmarkMetadata.launchTemplate(benchmarkTag))
              .build())
          .tagSpecifications(TagSpecification.builder()
              .tags(tags)
              .resourceType(ResourceType.INSTANCE)
              .build())
          .minCount(4)
          .maxCount(4)
          .build());
    } catch(Ec2Exception e) {
      System.out.println("Launch Instances Exception thrown: " + e.getMessage());
      System.exit(1);
    }

  }

  private static void createKeyPair(String benchmarkTag) throws IOException {
    // create keypair
    try {
      DescribeKeyPairsResponse
          dkpr = ec2.describeKeyPairs(
          DescribeKeyPairsRequest.builder().keyNames(AwsBenchmarkMetadata.keyPair(benchmarkTag)).build());
      System.out.println("SSH key pair for cluster '" + benchmarkTag + "' already exists!");
      System.exit(1);
    } catch(Ec2Exception e) {
      if (!e.getMessage().contains("The key pair '" + AwsBenchmarkMetadata.keyPair(benchmarkTag) + "' does not exist")) {
        System.out.println("Exception thrown: " + e.getMessage());
        System.exit(1);
      }
    }
    try {
      CreateKeyPairResponse
          ckpr = ec2.createKeyPair(
          CreateKeyPairRequest.builder().keyName(AwsBenchmarkMetadata.keyPair(benchmarkTag)).build());
      Files.createDirectories(Paths.get(BenchmarkMetadata.benchmarkKeyFileDirectory()));
      Files.write(Paths.get(AwsBenchmarkMetadata.keyPairFileName(benchmarkTag)), ckpr.keyMaterial().getBytes());
    } catch(Ec2Exception e) {
      System.out.println("Exception thrown: " + e.getMessage());
      System.exit(1);
    }
  }

  private static boolean createLaunchTemplate(String benchmarkTag, Image newestImage) {
    ArrayList<String> securityGroupList = new ArrayList<>();
    securityGroupList.add(AwsBenchmarkMetadata.securityGroup(benchmarkTag));

    // Create the launch template
    try {
      CreateLaunchTemplateResponse cltresponse =
          ec2.createLaunchTemplate(CreateLaunchTemplateRequest.builder()
              .launchTemplateName(AwsBenchmarkMetadata.launchTemplate(benchmarkTag))
              .launchTemplateData(RequestLaunchTemplateData.builder()
                  .imageId(newestImage.imageId())
                  .instanceType(AwsBenchmarkMetadata.instanceType())
                  .placement(LaunchTemplatePlacementRequest.builder()
                      .groupName(AwsBenchmarkMetadata.placementGroup(benchmarkTag))
                      .tenancy(AwsBenchmarkMetadata.tenancy())
                      .build())
                  .keyName(AwsBenchmarkMetadata.keyPair(benchmarkTag))
                  .securityGroups(securityGroupList)
                  .build())
              .build());

      System.out.println("Launch Template for cluster '" + benchmarkTag + "' created.");
    } catch(Ec2Exception e) {
      System.out.println("Exception thrown: " + e.getMessage());
      return false;
    }
    return true;
  }

  private static boolean createSecurityGroup(String benchmarkTag, List<Tag> tags) {
    // Make a security group for the launch template
    try {
      CreateSecurityGroupResponse csgr = ec2.createSecurityGroup(CreateSecurityGroupRequest.builder()
          .groupName(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
          .description(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
          .build());
      String groupId = csgr.groupId();
      ec2.createTags(CreateTagsRequest.builder().resources(groupId).tags(tags).build());
      System.out.println("Security Group for cluster '" + benchmarkTag + "' created.");

      // Allow all members of the security group to freely talk to each other
      ec2.authorizeSecurityGroupIngress(AuthorizeSecurityGroupIngressRequest.builder()
          .groupName(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
          .sourceSecurityGroupName(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
          .build());
      ec2.authorizeSecurityGroupIngress(AuthorizeSecurityGroupIngressRequest.builder()
          .groupName(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
          .cidrIp("0.0.0.0/0")
          .ipProtocol("tcp")
          .toPort(22)
          .fromPort(22)
          .build());
      System.out.println("Security Group permissions for cluster '" + benchmarkTag + "' set.");
    } catch(Ec2Exception e) {
      String message = e.getMessage();
      if (message.contains("'" + AwsBenchmarkMetadata.securityGroup(benchmarkTag) + "' already exists")) {
        System.out.println("Security group for tag " + benchmarkTag + " already exists. Cowardly refusing to continue.");
      }
      else {
        System.out.println("Exception thrown: " + e.getMessage());
      }
      return false;
    }
    return true;
  }

  private static boolean createPlacementGroup(String benchmarkTag) {
    // Make a placement group for the launch template
    try {
      CreatePlacementGroupResponse cpgr = ec2.createPlacementGroup(CreatePlacementGroupRequest.builder()
              .groupName(AwsBenchmarkMetadata.placementGroup(benchmarkTag))
              .strategy(AwsBenchmarkMetadata.placementGroupStrategy())
              .build());
//      ec2.createTags(CreateTagsRequest.builder().resources().build())
      System.out.println("Placement Group for cluster '" + benchmarkTag + "' created.");
    } catch(Ec2Exception e) {
      System.out.println("Exception thrown: " + e.getMessage());
      return false;
    }
    return true;
  }

  private static List<Tag> getTags(String benchmarkTag) {
    // Create tags for everything
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.builder().key("purpose").value(BenchmarkMetadata.prefix).build());
    tags.add(Tag.builder().key(BenchmarkMetadata.prefix).value(benchmarkTag).build());
    return tags;
  }

  private static Image getNewestImage() {
    DateTimeFormatter
        inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    // Find an appropriate AMI to launch our cluster with
    List<Image> benchmarkImages = ec2.describeImages(
        DescribeImagesRequest
            .builder()
            .filters(Filter.builder().name("tag:purpose").values("geode-benchmarks").build())
            .build()).images();

    // benchmarkImages is an immutable list so we have to copy it
    List<Image>sortableImages = new ArrayList<>(benchmarkImages);
    sortableImages.sort(
        Comparator.comparing((Image i) -> LocalDateTime.parse(i.creationDate(), inputFormatter)));
    if (sortableImages.size() < 1) {
      System.out.println("No suitable AMIs available, exiting.");
      System.exit(1);
    }
    return sortableImages.get(sortableImages.size() - 1);
  }
}
