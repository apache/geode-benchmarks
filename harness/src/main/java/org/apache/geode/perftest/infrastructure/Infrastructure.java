package org.apache.geode.perftest.infrastructure;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Set;

/**
 * Some deployed infrastructure that the test is running on
 */
public interface Infrastructure extends AutoCloseable {

  /**
   * Get the nodes that the test is running on
   */
  Set<Node> getNodes();


  int onNode(Node node, String[] shellCommand)
      throws IOException, InterruptedException;

  /**
   * Delete the nodes
   */
  void close() throws InterruptedException, IOException;

  /**
   * Copy a list of files to a directory on the node
   */
  void copyToNodes(Iterable<File> files, String destDir) throws IOException;

  void copyFromNode(Node node, String directory, File destDir) throws IOException;

  interface Node extends Serializable {

    InetAddress getAddress();
  }
}
