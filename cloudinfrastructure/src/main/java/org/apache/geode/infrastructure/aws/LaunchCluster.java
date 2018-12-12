package org.apache.geode.infrastructure.aws;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressResponse;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateResponse;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.LaunchTemplatePlacementRequest;
import software.amazon.awssdk.services.ec2.model.RequestLaunchTemplateData;
import software.amazon.awssdk.services.ec2.model.Tenancy;


public class LaunchCluster {

  public static void main(String[] args) throws IOException {
    boolean valid = true;
    if (args.length != 1) {
      System.exit(1);
      return;
    }
    String benchmarkTag = args[0];
    final String benchmarkPrefix = "geode-benchmarks";

    if (benchmarkTag == null || benchmarkTag.isEmpty()) {
      valid = false;
    }

    if (!valid) {
      System.exit(1);
      return;
    }

    DateTimeFormatter
        inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    Ec2Client ec2 = Ec2Client.create();
    List<Image> benchmarkImages = ec2.describeImages(
        DescribeImagesRequest
            .builder()
            .filters(Filter.builder().name("tag:purpose").values("geode-benchmarks").build())
            .build()).images();

    List<Image>sortableImages = new ArrayList<>(benchmarkImages);
    sortableImages.sort(
        Comparator.comparing((Image i) -> LocalDateTime.parse(i.creationDate(), inputFormatter)));
    Image newestImage = sortableImages.get(sortableImages.size() - 1);

    final String benchmarkTagPrefix = benchmarkPrefix + "-" + benchmarkTag;
    final String launchTemplateName = benchmarkTagPrefix + "-launch-template";
    final InstanceType instanceType = InstanceType.C5_9_XLARGE;
    final String placementGroup = benchmarkTagPrefix + "-placement-group";
    final Tenancy tenancy = Tenancy.DEDICATED;
    final String securityGroupName = benchmarkTagPrefix + "-security-group";
    try {
      ec2.createSecurityGroup(CreateSecurityGroupRequest.builder()
          .groupName(securityGroupName)
          .description(securityGroupName)
          .build());
      System.out.println("Security Group for cluster '" + benchmarkTag + "' created.");
      ec2.authorizeSecurityGroupIngress(AuthorizeSecurityGroupIngressRequest.builder()
          .groupName(securityGroupName)
          .sourceSecurityGroupName(securityGroupName)
          .build());
      System.out.println("Security Group permissions for cluster '" + benchmarkTag + "' created.");
    } catch(Ec2Exception e) {
      String message = e.getMessage();
      if (message.contains("'" + securityGroupName + "' already exists")) {
        System.out.println("Security group for tag " + benchmarkTag + " already exists. Cowardly refusing to continue.");
      }
      else {
        System.out.println("Exception thrown: " + e.getMessage());
      }
      System.exit(1);
      return;
    }
    ArrayList<String> securityGroupList = new ArrayList<>();
    securityGroupList.add(securityGroupName);
    try {
      CreateLaunchTemplateResponse cltresponse =
          ec2.createLaunchTemplate(CreateLaunchTemplateRequest.builder()
              .launchTemplateName(launchTemplateName)
              .launchTemplateData(RequestLaunchTemplateData.builder()
                  .imageId(newestImage.imageId())
                  .instanceType(instanceType)
                  .placement(LaunchTemplatePlacementRequest.builder()
                      .groupName(placementGroup)
                      .tenancy(tenancy)
                      .build())
                  .securityGroups(securityGroupList)
                  .build())
              .build());

      System.out.println("Created launch template: " + cltresponse.launchTemplate().launchTemplateName());
    } catch(Ec2Exception e) {
      System.out.println("Exception thrown: " + e.getMessage());
    }
  }
}
