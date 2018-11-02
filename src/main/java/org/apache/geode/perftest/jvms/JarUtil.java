package org.apache.geode.perftest.jvms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;

public class JarUtil {
  static void jar(File file, File outputFile) throws IOException {
    Manifest manifest = new Manifest();
    try (JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(outputFile), manifest)) {
      Path start = file.toPath();

      Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
          if(file.toFile().isDirectory()) {
            return FileVisitResult.CONTINUE;
          }

          JarEntry entry = new JarEntry(start.relativize(file).toString());
          outputStream.putNextEntry(entry);
          try (FileInputStream input = new FileInputStream(file.toFile())) {
            IOUtils.copy(input, outputStream);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    }

    System.out.println("Contents = " + outputFile.getPath());
  }

}
