package org.apache.geode.perftest.infrastructure;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * A description of deployed infrastructure that the test is running on
 */
public interface Infrastructure {

  Set<Node> getNodes();

  void onNode(Node node, String[] shellCommand) throws IOException;

  void delete() throws InterruptedException;

  void copyFiles(Iterable<File> files, String destDir) throws IOException;

  interface Node {

  }
}
