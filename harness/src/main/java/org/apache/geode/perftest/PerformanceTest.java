package org.apache.geode.perftest;

import java.io.Serializable;

import org.yardstickframework.BenchmarkDriver;

/**
 * A declarative performance test. Users should implement
 * this interface and define their test using the passed in
 * {@link TestConfig} object.
 *
 * There are three phases to the test:
 * <ul>
 *   <li> Before Tasks - executed sequentially before the test</li>
 *   <li> Workload Tasks - executed in parallel repeatedly during the workload phase </li>
 *   <li> After Tasks - executed sequentially after the test</li>
 * </ul>
 *
 * Each of these phases can be assigned to *roles*.
 *
 * The test should, at a minimum
 *
 * Define the roles by calling {@link TestConfig#role(String, int)}
 * Define one or more tasks by calling {@link TestConfig#before(Task, String...)},
 * {@link TestConfig#after(Task, String...)} or {@link TestConfig#workload(BenchmarkDriver, String...)}
 */
public interface PerformanceTest extends Serializable {

  /**
   * Return the configuration for the test.
   * @param test
   */
  void configure(TestConfig test);

}
