package org.apache.geode.infrastructure.aws;

import org.apache.geode.infrastructure.BenchmarkMetadata;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.PlacementStrategy;
import software.amazon.awssdk.services.ec2.model.Tenancy;

class AwsBenchmarkMetadata implements BenchmarkMetadata {
    static InstanceType instanceType = InstanceType.C5_9_XLARGE;
    static Tenancy tenancy = Tenancy.DEDICATED;
    static String securityGroup(String tag) {
        return(BenchmarkMetadata.benchmarkString(tag, "securityGroup"));
    }

    static String placementGroup(String tag) {
        return(BenchmarkMetadata.benchmarkString(tag, "placementGroup"));
    }

    static String launchTemplate(String tag) {
        return(BenchmarkMetadata.benchmarkString(tag, "launchTemplate"));
    }

    static String keyPair(String tag) {
        return(BenchmarkMetadata.benchmarkString(tag, "keyPair"));
    }

    static String keyPairFileName(String tag) {
        return(BenchmarkMetadata.benchmarkKeyFileName(tag));
    }

    static InstanceType instanceType() {
        return(instanceType);
    }

    static Tenancy tenancy() {
        return(tenancy);
    }

    static PlacementStrategy placementGroupStrategy() {
        return PlacementStrategy.CLUSTER;
    }
}
