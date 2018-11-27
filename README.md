# Apache Geode Benchmarks

This project contains a benchmarking framework and benchmarks for 
[Apache Geode](https://geode.apache.org/). It is based on the 
[yardstick framework](https://github.com/gridgain/yardstick), but with a java based
test configuration and test execution framework.

These benchmarks are under development.

## Running the benchmarks
The benchmarks require machines with passwordless ssh enabled in order to run.

To run all benchmarks, run the benchmark task and pass in a list of hosts.

For example:
```
./gradlew benchmark -Phosts=localhost,localhost,localhost -PoutputDir=/tmp/results
```

### Running in google cloud

This project includes some scripts to automate running benchmarks in google cloud. See the 
[README.md](infrastructure/google_cloud/README.md) in the infrastructure/google_cloud directory.

## Project structure

The project is divided into two modules
* harness - Contains test framework code for running benchmarks. Does not depend on Apache Geode.
* geode-benchmarks - Individual benchmarks of Apache Geode.

## Sample benchmark

Benchmarks are defined in a declarative configuration classes. Each configuration class is run as 
a junit test which calls the configure method and passes it to the TestRunner, which executes
the test on the provided TEST_HOSTS.

Benchmarks are composed of `before tasks`, `after tasks`, and `workload tasks`. The `before` and `after`
tasks are run once. `Workload` tasks are run repeatedly and their execution time is measured and
reported by the yardstick framework. 

```java
/**
* Benchmark configuration class, which defines the topology of the test and
* the initialization tasks and workload tasks for the test.
*/
public class PartitionedPutBenchmark {

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this::configure);
  }
  
  /**
  * Declare the configuration of the test by calling methods
  * on TestConfig.
  */
  public void configure(TestConfig config) {

    int locatorPort = 10334;

    //This test has three roles, a geode locator, server, and client
    config.role("locator", 1);
    config.role("server", 1);
    config.role("client", 1);
    
    //Define how the locator,server and client are initialized
    config.before(new StartLocator(locatorPort), "locator");
    config.before(new StartServer(locatorPort), "server");
    config.before(new StartClient(locatorPort), "client");
    
    //Define the benchmarked workload, which runs in a client
    config.workload(new PutTask());
  }
}
```

```java
/**
* Workload task, which extends the yardstick BenchmarkDriverAdapter
* 
* Workload tasks should execute a single unit of work, and will be run repeatedly
* for the duration of the test.
*/
public class PutTask extends BenchmarkDriverAdapter implements Serializable {
  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    region.put(1,2);
    return true;
  }
}
```
