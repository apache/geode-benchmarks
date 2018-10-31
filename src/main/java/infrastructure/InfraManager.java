package infrastructure;

import java.io.IOException;

import org.jclouds.compute.RunNodesException;
import suite.Suite;

public interface InfraManager {

  public Infrastructure create() throws RunNodesException, IOException;

}
