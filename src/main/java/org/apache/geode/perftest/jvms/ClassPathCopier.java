package org.apache.geode.perftest.jvms;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.geode.perftest.infrastructure.Infrastructure;

public class ClassPathCopier {
  public String classpath;

  public ClassPathCopier(String classpath) {
    this.classpath = classpath;
  }

  public void copyToNodes(Infrastructure infrastructure) throws IOException {
    String[] fileArray = classpath.split(File.pathSeparator);

    String destDir = "lib";
    Iterable<File> files = Arrays.asList(fileArray).stream().map(File::new).collect(Collectors.toList());
    infrastructure.copyFiles(files, destDir);
  }
}
