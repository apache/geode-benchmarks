package org.apache.geode.infrastructure.aws;


import static java.lang.Thread.sleep;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
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
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
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

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 2) {
      usage("Usage: LaunchCluster <tag> <count>");
      return;
    }
    String benchmarkTag = args[0];
    int count = Integer.parseInt(args[1]);

    if (benchmarkTag == null || benchmarkTag.isEmpty()) {
      usage("Usage: LaunchCluster <tag> <count>");
    }

    List<Tag> tags = getTags(benchmarkTag);
    createKeyPair(benchmarkTag);
    Image newestImage = getNewestImage();

    createPlacementGroup(benchmarkTag);
    createSecurityGroup(benchmarkTag, tags);
    createLaunchTemplate(benchmarkTag, newestImage);

    List<String> instanceIds = launchInstances(benchmarkTag, tags, count);
    DescribeInstancesResponse instances = waitForInstances(instanceIds);

    List<String> publicIps = installPrivateKey(benchmarkTag, instances);

    System.out.println("Instances successfully launched! Public IPs: " + publicIps);
  }

  private static void usage(String s) {
    throw new IllegalStateException(s);
  }

  private static List<String> launchInstances(String benchmarkTag, List<Tag> tags,
      int instanceCount)
      throws InterruptedException {
    // launch instances

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

    List<String> instanceIds = rir.instances()
        .stream()
        .map(Instance::instanceId)
        .collect(Collectors.toList());

    return instanceIds;
  }

  private static DescribeInstancesResponse waitForInstances(List<String> instanceIds)
      throws InterruptedException {
    System.out.println("Waiting for cluster instances to go fully online.");

    DescribeInstancesResponse describeInstancesResponse = describeInstances(instanceIds, "running");
    while (instanceCount(describeInstancesResponse) < instanceIds.size()) {
      sleep(AwsBenchmarkMetadata.POLL_INTERVAL);
      System.out.println("Continuing to wait.");
      describeInstancesResponse = describeInstances(instanceIds, "running");
    }

    return describeInstancesResponse;
  }

  private static List<String> installPrivateKey(String benchmarkTag,
      DescribeInstancesResponse describeInstancesResponse) {
    List<String> publicIps =
        describeInstancesResponse.reservations().stream()
            .flatMap(reservation -> reservation.instances().stream())
            .map(Instance::publicIpAddress).collect(Collectors.toList());

    new KeyInstaller(AwsBenchmarkMetadata.USER,
        Paths.get(AwsBenchmarkMetadata.keyPairFileName(benchmarkTag))).installPrivateKey(publicIps);

    System.out.println("Private key installed on all instances for passwordless ssh");

    return publicIps;
  }

  private static long instanceCount(DescribeInstancesResponse describeInstancesResponse) {
    return describeInstancesResponse
        .reservations().stream().flatMap(reservation -> reservation.instances().stream()).count();
  }

  private static DescribeInstancesResponse describeInstances(List<String> instanceIds,
      String state) {
    return ec2.describeInstances(DescribeInstancesRequest.builder()
        .instanceIds(instanceIds)
        .filters(Filter.builder()
            .name("instance-state-name")
            .values(state)
            .build())
        .build());
  }

  private static void createKeyPair(String benchmarkTag) throws IOException {
    CreateKeyPairResponse ckpr = ec2.createKeyPair(
        CreateKeyPairRequest.builder().keyName(AwsBenchmarkMetadata.keyPair(benchmarkTag)).build());
    Files.createDirectories(Paths.get(BenchmarkMetadata.benchmarkKeyFileDirectory()));
    Path privateKey = Files.write(Paths.get(AwsBenchmarkMetadata.keyPairFileName(benchmarkTag)),
        ckpr.keyMaterial().getBytes());
    Files.setPosixFilePermissions(privateKey, PosixFilePermissions.fromString("rw-------"));
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
    DateTimeFormatter inputFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    // Find an appropriate AMI to launch our cluster with
    List<Image> benchmarkImages = ec2.describeImages(
        DescribeImagesRequest
            .builder()
            .filters(Filter.builder().name("tag:purpose").values("geode-benchmarks").build())
            .build())
        .images();

    // benchmarkImages is an immutable list so we have to copy it
    List<Image> sortableImages = new ArrayList<>(benchmarkImages);
    sortableImages.sort(
        Comparator.comparing((Image i) -> LocalDateTime.parse(i.creationDate(), inputFormatter)));
    if (sortableImages.size() < 1) {
      System.out.println("No suitable AMIs available, exiting.");
      System.exit(1);
    }
    return sortableImages.get(sortableImages.size() - 1);
  }
}
