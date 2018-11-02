package org.apache.geode.perftest.jvms;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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
    Iterable<File> files = Arrays.asList(fileArray)
        .stream()
        .map(File::new)
        .map(this::jarDir)
        .collect(Collectors.toSet());



    infrastructure.copyFiles(files, destDir);
  }

  private File jarDir(File file) {
    if(!file.isDirectory()) {
      return file;
    }

    try {
      File
          outputFile =
          new File(System.getProperty("java.io.tmpdir"), Math.abs(file.hashCode()) + "_" + file.getName() + ".jar");
      outputFile.deleteOnExit();

      JarUtil.jar(file, outputFile);

      return outputFile;
    } catch(IOException e) {
      throw new UncheckedIOException(e);
    }

  }

}
