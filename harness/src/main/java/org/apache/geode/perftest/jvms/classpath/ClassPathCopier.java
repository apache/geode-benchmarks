package org.apache.geode.perftest.jvms.classpath;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.geode.perftest.infrastructure.Infrastructure;

/**
 * Utility for making sure that all nodes in the infrastructure can have the same classpath
 * that the controller JVM has.
 */
public class ClassPathCopier {

  private final String javaHome;
  public String classpath;

  public ClassPathCopier(String classpath, String javaHome) {
    this.classpath = classpath;
    this.javaHome = javaHome;
  }

  /**
   * Copy the current classpath to a lib directory on all of the nodes in the infrastructure
   */
  public void copyToNodes(Infrastructure infrastructure) throws IOException {
    String[] fileArray = classpath.split(File.pathSeparator);

    String destDir = "lib";
    Iterable<File> files = Arrays.asList(fileArray)
        .stream()
        .filter(path -> !path.contains(javaHome))
        .map(File::new)
        .map(this::jarDir)
        .filter(File::exists)
        .collect(Collectors.toSet());

    infrastructure.copyToNodes(files, destDir);
  }

  private File jarDir(File file) {
    if(!file.isDirectory()) {
      return file;
    }

    try {
      File outputFile =
          new File(System.getProperty("java.io.tmpdir"), Math.abs(file.hashCode()) + "_" + file.getName() + ".jar");

      outputFile.deleteOnExit();

      JarUtil.jar(file, outputFile);

      return outputFile;
    } catch(IOException e) {
      throw new UncheckedIOException(e);
    }

  }

}
