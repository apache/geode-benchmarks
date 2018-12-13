package org.apache.geode.infrastructure;

/**
 * Static methods to generate common strings used by the infrastructure.
 */
public class BenchmarkMetadata {
    public static String PREFIX = "geode-benchmarks";
    public static String SSH_DIRECTORY = ".ssh/geode-benchmarks";

    public static String benchmarkPrefix(String tag) {
        return PREFIX + "-" + tag;
    }

    public static String benchmarkString(String tag, String suffix) {
        return benchmarkPrefix(tag) + "-" + suffix;
    }

    public static String benchmarkKeyFileDirectory() {
        return System.getProperty("user.home") + "/" + SSH_DIRECTORY;
    }

    public static String benchmarkKeyFileName(String tag) {
        return benchmarkKeyFileDirectory() + "/" + tag + ".pem";
    }
}
