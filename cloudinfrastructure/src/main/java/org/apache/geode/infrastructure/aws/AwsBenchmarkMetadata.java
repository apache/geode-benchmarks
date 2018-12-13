package org.apache.geode.infrastructure.aws;

import org.apache.geode.infrastructure.BenchmarkMetadata;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.PlacementStrategy;
import software.amazon.awssdk.services.ec2.model.Tenancy;

/**
 * Static methods to generate common strings used for AWS infrastructure.
 */
class AwsBenchmarkMetadata extends BenchmarkMetadata {
    public static InstanceType INSTANCE_TYPE = InstanceType.C5_9_XLARGE;
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
        return BenchmarkMetadata.benchmarkKeyFileName(tag);
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
