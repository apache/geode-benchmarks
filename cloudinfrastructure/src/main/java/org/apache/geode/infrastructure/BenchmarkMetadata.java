package org.apache.geode.infrastructure;

public interface BenchmarkMetadata {
    String prefix = "geode-benchmarks";
    String sshDirectory = ".ssh/geode-benchmarks";

    static String benchmarkPrefix(String tag) {
        return(prefix + "-" + tag);
    }

    static String benchmarkString(String tag, String suffix) {
        return(benchmarkPrefix(tag) + "-" + suffix);
    }

    static String benchmarkKeyFileDirectory() {
        return(System.getProperty("user.home") + "/" + sshDirectory);
    }

    static String benchmarkKeyFileName(String tag) {
        return(benchmarkKeyFileDirectory() + "/" + tag + ".pem");
    }
}
