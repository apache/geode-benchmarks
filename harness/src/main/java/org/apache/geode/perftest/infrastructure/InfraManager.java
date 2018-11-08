package org.apache.geode.perftest.infrastructure;

/**
 * Interface for creating or providing access a number of nodes on a given infrastructure
 */
public interface InfraManager {

  Infrastructure create(int nodes) throws Exception;

}
