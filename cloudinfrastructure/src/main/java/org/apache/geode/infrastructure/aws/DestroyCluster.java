package org.apache.geode.infrastructure.aws;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateResponse;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DeleteLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.LaunchTemplatePlacementRequest;
import software.amazon.awssdk.services.ec2.model.RequestLaunchTemplateData;
import software.amazon.awssdk.services.ec2.model.Tenancy;


public class DestroyCluster {

  public static void main(String[] args) throws IOException {
    boolean valid = true;
    if (args.length != 1) {
      System.out.println("This takes one argument, the cluster tag to work with.");
      System.exit(1);
      return;
    }
    String benchmarkTag = args[0];
    final String benchmarkPrefix = "geode-benchmarks";

    if (benchmarkTag == null || benchmarkTag.isEmpty()) {
      valid = false;
    }

    if (!valid) {
      System.out.println("No valid tag found.");
      System.exit(1);
      return;
    }


    Ec2Client ec2 = Ec2Client.create();
    final String benchmarkTagPrefix = benchmarkPrefix + "-" + benchmarkTag;
    final String launchTemplateName = benchmarkTagPrefix + "-launch-template";
    final InstanceType instanceType = InstanceType.C5_9_XLARGE;
    final String placementGroup = benchmarkTagPrefix + "-placement-group";
    final Tenancy tenancy = Tenancy.DEDICATED;
    final String securityGroupName = benchmarkTagPrefix + "-security-group";
    try {
      ec2.deleteLaunchTemplate(DeleteLaunchTemplateRequest.builder()
          .launchTemplateName(launchTemplateName)
          .build());

      System.out.println("Launch template for cluster '" + benchmarkTag + "' deleted.");
    } catch(Ec2Exception e) {
      System.out.println("We got an exception while deleting the launch template");
    }
    try {
      ec2.deleteSecurityGroup(DeleteSecurityGroupRequest.builder()
          .groupName(securityGroupName)
          .build());

      System.out.println("Security Group for cluster '" + benchmarkTag + "' deleted.");
    } catch(Ec2Exception e) {
      System.out.println("We got an exception while deleting the security group");

      // we don't care what happens.
    }
  }
}
