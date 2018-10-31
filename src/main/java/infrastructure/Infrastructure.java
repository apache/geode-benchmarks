package infrastructure;

import java.util.Set;

import org.jclouds.compute.domain.NodeMetadata;

/**
 * A description of deployed infrastructure that the test is running on
 */
public interface Infrastructure {

  Set<? extends NodeMetadata> getNodes();

  String onNode(NodeMetadata node, String hostname);

  void delete();
}
