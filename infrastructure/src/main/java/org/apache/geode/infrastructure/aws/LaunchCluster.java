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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AllocateHostsRequest;
import software.amazon.awssdk.services.ec2.model.AllocateHostsResponse;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateResponse;
import software.amazon.awssdk.services.ec2.model.CreatePlacementGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.CreateTagsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateBlockDeviceMappingRequest;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateEbsBlockDeviceRequest;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateSpecification;
import software.amazon.awssdk.services.ec2.model.Placement;
import software.amazon.awssdk.services.ec2.model.RequestLaunchTemplateData;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagSpecification;
import software.amazon.awssdk.services.ec2.model.Tenancy;
import software.amazon.awssdk.services.ec2.model.VolumeType;

import org.apache.geode.infrastructure.BenchmarkMetadata;

public class LaunchCluster {
  private static final long MAX_WAIT_INTERVAL = 2000;
  private static final int MAX_DESCRIBE_RETRIES = 5;
  private static final int MAX_CREATE_RETRIES = 3;
  private static final int MAX_TAG_RETRIES = 3;
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
    authorizeSecurityGroup(benchmarkTag);
    createLaunchTemplate(benchmarkTag, newestImage);

    int ec2Timeout = 300;
    List<String> hostIds = allocateHosts(tags, count, ec2Timeout);
    List<String> instanceIds = launchInstances(benchmarkTag, tags, hostIds);
    DescribeInstancesResponse instances = waitForInstances(instanceIds, ec2Timeout);
    List<String> publicIps = getPublicIps(instances);
    createMetadata(benchmarkTag, publicIps);
    installPrivateKey(benchmarkTag, publicIps);
    installMetadata(benchmarkTag, publicIps);

    System.out.println("Instances successfully launched! Public IPs: " + publicIps);
  }

  private static List<String> getPublicIps(DescribeInstancesResponse describeInstancesResponse) {
    return describeInstancesResponse.reservations().stream()
        .flatMap(reservation -> reservation.instances().stream())
        .map(Instance::publicIpAddress).collect(Collectors.toList());
  }

  private static void usage(String s) {
    throw new IllegalStateException(s);
  }

  private static List<String> allocateHosts(List<Tag> tags, int count, int timeout)
      throws InterruptedException {
    int gotHosts = 0;
    AllocateHostsResponse hosts;
    List<String> hostIds = new ArrayList<>();

    Instant end = Instant.now().plus(Duration.ofSeconds(timeout));
    do {
      hosts = ec2.allocateHosts(AllocateHostsRequest.builder()
          .availabilityZone("us-west-2a")
          .instanceType(AwsBenchmarkMetadata.instanceType().toString())
          .quantity(count - gotHosts)
          .tagSpecifications(TagSpecification.builder()
              .tags(tags)
              .resourceType(ResourceType.DEDICATED_HOST)
              .build())
          .build());
      hostIds.addAll(hosts.hostIds());
      gotHosts += hosts.hostIds().size();
      if (Instant.now().isAfter(end)) {
        throw new InterruptedException(
            count + " hosts were not allocated before timeout of " + timeout + " seconds.");
      }
    } while (gotHosts < count);

    return hostIds;
  }

  private static List<String> launchInstances(String launchTemplate, List<Tag> tags,
      List<String> hosts) {
    List<String> instanceIds = new ArrayList<>(hosts.size());
    for (String host : hosts) {
      RunInstancesResponse rir = ec2.runInstances(RunInstancesRequest.builder()
          .launchTemplate(LaunchTemplateSpecification.builder()
              .launchTemplateName(AwsBenchmarkMetadata.launchTemplate(launchTemplate))
              .build())
          .placement(Placement.builder()
              .tenancy(Tenancy.HOST)
              .hostId(host)
              .build())
          .tagSpecifications(TagSpecification.builder()
              .tags(tags)
              .resourceType(ResourceType.INSTANCE)
              .build())
          .minCount(1)
          .maxCount(1)
          .build());

      instanceIds.add(rir.instances().get(0).instanceId());
    }

    return instanceIds;
  }

  private static DescribeInstancesResponse waitForInstances(List<String> instanceIds, int timeout)
      throws InterruptedException {
    System.out.println("Waiting for cluster instances to go fully online.");

    Instant end = Instant.now().plus(Duration.ofSeconds(timeout));
    DescribeInstancesResponse describeInstancesResponse;
    do {
      sleep(AwsBenchmarkMetadata.POLL_INTERVAL);
      System.out.println(
          "Continuing to wait for " + new StringBuilder().append(instanceIds + ", ").toString());
      describeInstancesResponse = describeInstances(instanceIds, "running");
      if (Instant.now().isAfter(end)) {
        throw new InterruptedException(instanceIds.size()
            + " hosts were not running before timeout of " + timeout + " seconds.");
      }
    } while (instanceCount(describeInstancesResponse) < instanceIds.size());

    return describeInstancesResponse;
  }

  private static void installPrivateKey(String benchmarkTag,
      List<String> publicIps) {
    new KeyInstaller(benchmarkTag).installPrivateKey(publicIps);
    System.out.println("Private key installed on all instances for passwordless ssh");
  }

  private static void installMetadata(String benchmarkTag,
      List<String> publicIps) {
    new MetadataInstaller(benchmarkTag).installMetadata(publicIps);
    System.out.println("Instance ID information installed on all instances");
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
    Path configDirectory = Paths.get(BenchmarkMetadata.benchmarkConfigDirectory());
    CreateKeyPairResponse ckpr = ec2.createKeyPair(
        CreateKeyPairRequest.builder().keyName(AwsBenchmarkMetadata.keyPair(benchmarkTag)).build());

    if (!configDirectory.toFile().exists()) {
      Files.createDirectories(Paths.get(BenchmarkMetadata.benchmarkConfigDirectory()));
    }
    Path privateKey = Files.write(Paths.get(AwsBenchmarkMetadata.keyPairFileName(benchmarkTag)),
        ckpr.keyMaterial().getBytes());
    Files.setPosixFilePermissions(privateKey, PosixFilePermissions.fromString("rw-------"));
  }

  private static void createMetadata(String benchmarkTag, List<String> publicIps)
      throws IOException {
    UUID instanceId = UUID.randomUUID();
    // TODO - Filter out only benchmark properties from system properties? Maybe not necessary.
    Properties metadata = new Properties(System.getProperties());
    metadata.setProperty("benchmark.instanceId", instanceId.toString());
    metadata.setProperty("benchmark.publicIps", String.join(",", publicIps));
    Path configDirectory = Paths.get(BenchmarkMetadata.benchmarkConfigDirectory());

    if (!configDirectory.toFile().exists()) {
      Files.createDirectories(Paths.get(BenchmarkMetadata.benchmarkConfigDirectory()));
    }

    Path metadataPath = Paths.get(AwsBenchmarkMetadata.metadataFileName(benchmarkTag));
    try (FileWriter writer = new FileWriter(metadataPath.toFile())) {
      metadata.store(writer, "Benchmark metadata generated during cluster launch");
    }
    Files.setPosixFilePermissions(metadataPath, PosixFilePermissions.fromString("rw-------"));
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
                .keyName(AwsBenchmarkMetadata.keyPair(benchmarkTag))
                .securityGroups(securityGroupList)
                .blockDeviceMappings(LaunchTemplateBlockDeviceMappingRequest.builder()
                    .deviceName("/dev/sda1")
                    .ebs(LaunchTemplateEbsBlockDeviceRequest.builder()
                        .volumeType(VolumeType.GP2)
                        .volumeSize(50)
                        .build())
                    .build())
                .build())
            .build());

    System.out.println("Launch Template for cluster '" + benchmarkTag + "' created.");
  }

  /*
   * Create the security group and wait until it is visible to subsequent commands.
   * This avoids issues caused by Amazon EC2 API eventual consistency model.
   */
  private static void createSecurityGroup(String benchmarkTag, List<Tag> tags)
      throws InterruptedException {
    CreateSecurityGroupResponse csgr = null;
    String groupId;

    for (int create_retries = 0; create_retries < MAX_CREATE_RETRIES; create_retries++) {
      try {
        csgr =
            ec2.createSecurityGroup(CreateSecurityGroupRequest.builder()
                .groupName(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
                .description(AwsBenchmarkMetadata.securityGroup(benchmarkTag))
                .build());
        break;
      } catch (Exception exception) {
        // try again
      }
    }

    if (csgr == null) {
      throw new RuntimeException(
          "Security Group was not created after " + MAX_CREATE_RETRIES + " attempts.");
    }

    groupId = csgr.groupId();
    DescribeSecurityGroupsRequest describeSecurityGroupsRequest =
        DescribeSecurityGroupsRequest.builder().groupIds(groupId).build();

    DescribeSecurityGroupsResponse describeSecurityGroupsResponse = null;
    for (int describeRetries = 0; describeRetries < MAX_DESCRIBE_RETRIES; describeRetries++) {
      try {
        describeSecurityGroupsResponse = ec2.describeSecurityGroups(describeSecurityGroupsRequest);

        if (!describeSecurityGroupsResponse.securityGroups().isEmpty()) {
          System.out.println("Security Group with id '" + groupId
              + "' is created and visible to subsequent commands.");
          break;
        }
      } catch (Ec2Exception exception) {
        // try again
      }
      Thread.sleep(Math.min(getWaitTimeExp(describeRetries), MAX_WAIT_INTERVAL));
    }

    if (describeSecurityGroupsResponse == null) {
      throw new RuntimeException("Security Group with id '" + groupId + "' was not visible after "
          + MAX_DESCRIBE_RETRIES + " attempts;");
    }

    CreateTagsResponse createTagResponse = null;
    for (int tagRetries = 0; tagRetries < MAX_TAG_RETRIES; tagRetries++) {
      try {
        createTagResponse =
            ec2.createTags(CreateTagsRequest.builder().resources(groupId).tags(tags).build());

        if (createTagResponse != null) {
          System.out.println("Tags for cluster '" + benchmarkTag + "' created.");
          break;
        }
      } catch (Exception exception) {
        // try again
      }
    }

    if (createTagResponse == null) {
      throw new RuntimeException("Tags for cluster '" + benchmarkTag + "' were not created after "
          + MAX_TAG_RETRIES + " attempts.");
    }

  }

  /*
   * Allow all members of the security group to freely talk to each other.
   */
  private static void authorizeSecurityGroup(String benchmarkTag) {
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
    tags.add(Tag.builder().key(BenchmarkMetadata.benchmarkString(benchmarkTag, "instanceId"))
        .value(UUID.randomUUID().toString()).build());
    return tags;
  }

  private static Image getNewestImage() {
    DateTimeFormatter inputFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    String purpose = System.getProperty("PURPOSE", "geode-benchmarks");

    if (purpose.isEmpty()) {
      purpose = "geode-benchmarks";
    }

    // Find an appropriate AMI to launch our cluster with
    List<Image> benchmarkImages = ec2.describeImages(
        DescribeImagesRequest
            .builder()
            .filters(Filter.builder().name("tag:purpose").values(purpose).build())
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

  /*
   * Returns the next wait interval, in milliseconds, using an exponential
   * backoff algorithm.
   */
  private static long getWaitTimeExp(int retryCount) {
    return ((long) Math.pow(2, retryCount) * 100L);
  }
}
