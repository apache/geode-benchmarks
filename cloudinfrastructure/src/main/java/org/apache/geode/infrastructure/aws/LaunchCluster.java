package org.apache.geode.infrastructure.aws;


import static java.lang.Thread.sleep;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateResponse;
import software.amazon.awssdk.services.ec2.model.CreatePlacementGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreatePlacementGroupResponse;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.LaunchTemplatePlacementRequest;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateSpecification;
import software.amazon.awssdk.services.ec2.model.RequestLaunchTemplateData;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagSpecification;

import org.apache.geode.infrastructure.BenchmarkMetadata;


public class LaunchCluster {
  static Ec2Client ec2 = Ec2Client.create();

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      throw new IllegalStateException("Usage: LaunchCluster <tag>");
    }
    String benchmarkTag = args[0];

    if (benchmarkTag == null || benchmarkTag.isEmpty()) {
      throw new IllegalStateException("Usage: LaunchCluster <tag>");
    }

    createKeyPair(benchmarkTag);
    Image newestImage = getNewestImage();
    List<Tag> tags = getTags(benchmarkTag);

    createPlacementGroup(benchmarkTag);
    createSecurityGroup(benchmarkTag, tags);
    createLaunchTemplate(benchmarkTag, newestImage);
    launchInstances(benchmarkTag, tags);
  }

  private static void launchInstances(String benchmarkTag, List<Tag> tags) {
    // launch instances

    int instanceCount = 4;

    try {
      RunInstancesResponse rir = ec2.runInstances(RunInstancesRequest.builder()
          .launchTemplate(LaunchTemplateSpecification.builder()
              .launchTemplateName(AwsBenchmarkMetadata.launchTemplate(benchmarkTag))
              .build())
          .tagSpecifications(TagSpecification.builder()
              .tags(tags)
              .resourceType(ResourceType.INSTANCE)
              .build())
          .minCount(instanceCount)
          .maxCount(instanceCount)
          .build());

      List<String>
          instanceIds = rir.instances()
              .stream()
          .map(Instance::instanceId)
          .collect(Collectors.toList());

      System.out.println("Waiting for cluster instances to go fully online.");

      while (ec2.describeInstances(DescribeInstancesRequest.builder()
          .instanceIds(instanceIds)
          .filters(Filter.builder()
              .name("instance-state-name")
              .values("running")
              .build())
          .build()).reservations().stream().flatMap(reservation -> reservation.instances().stream()).count() > instanceCount) {
        sleep(60000);
        System.out.println("Continuing to wait.");
      }
    } catch(Ec2Exception e) {
      System.out.println("Launch Instances Exception thrown: " + e.getMessage());
      System.exit(1);
    }
    catch(InterruptedException e) {
      System.out.println("InterruptedException caught");
    }
  }

  private static void createKeyPair(String benchmarkTag) throws IOException {
    // create keypair
//    try {
//      DescribeKeyPairsResponse
//          dkpr = ec2.describeKeyPairs(
//          DescribeKeyPairsRequest.builder().keyNames(AwsBenchmarkMetadata.keyPair(benchmarkTag)).build());
//      throw new IllegalStateException("SSH key pair for cluster '" + benchmarkTag + "' already exists!");
//    } catch(Ec2Exception e) {
//      if (!e.getMessage().contains("The key pair '" + AwsBenchmarkMetadata.keyPair(benchmarkTag) + "' does not exist")) {
//        throw e;
//      }
//    }
      CreateKeyPairResponse
          ckpr = ec2.createKeyPair(
          CreateKeyPairRequest.builder().keyName(AwsBenchmarkMetadata.keyPair(benchmarkTag)).build());
      Files.createDirectories(Paths.get(BenchmarkMetadata.benchmarkKeyFileDirectory()));
      Files.write(Paths.get(AwsBenchmarkMetadata.keyPairFileName(benchmarkTag)), ckpr.keyMaterial().getBytes());
  }

  private static void createLaunchTemplate(String benchmarkTag, Image newestImage) {
    ArrayList<String> securityGroupList = new ArrayList<>();
    securityGroupList.add(AwsBenchmarkMetadata.securityGroup(benchmarkTag));

    // Create the launch template
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
  }

  private static void createSecurityGroup(String benchmarkTag, List<Tag> tags) {
    // Make a security group for the launch template
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
  }

  private static void createPlacementGroup(String benchmarkTag) {
      ec2.createPlacementGroup(CreatePlacementGroupRequest.builder()
              .groupName(AwsBenchmarkMetadata.placementGroup(benchmarkTag))
              .strategy(AwsBenchmarkMetadata.placementGroupStrategy())
              .build());
      System.out.println("Placement Group for cluster '" + benchmarkTag + "' created.");
  }

  private static List<Tag> getTags(String benchmarkTag) {
    // Create tags for everything
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.builder().key("purpose").value(BenchmarkMetadata.PREFIX).build());
    tags.add(Tag.builder().key(BenchmarkMetadata.PREFIX).value(benchmarkTag).build());
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
