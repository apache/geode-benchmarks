[![Build Status](https://travis-ci.org/apache/geode-benchmarks.svg?branch=develop)](https://travis-ci.org/apache/geode-benchmarks)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/apache/geode-benchmarks.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/apache/geode-benchmarks/alerts/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)


# Apache Geode Benchmarks

This project contains a benchmarking framework and benchmarks for 
[Apache Geode](https://geode.apache.org/). It is based on the 
[yardstick framework](https://github.com/gridgain/yardstick), but with a java based
test configuration and test execution framework.

These benchmarks are under development.

## Running the benchmarks
The benchmarks require machines with passwordless ssh enabled in order to run.
So ensure that the authentication key pair for SSH does not have a passphrase. If you had 
already previously created a key pair with a passphrase, you can create a different key pair which
is of a different type than the previously created one.
Use one of the following commands to create a new key pair.
```
ssh-keygen -t ed25519
ssh-keygen -t rsa -b 4096
ssh-keygen -t dsa
ssh-keygen -t ecdsa -b 521
```
While runinng a test on a single machine (i.e. localhost) add the generated key to `authorized_keys` to authorize the user.
```
cat <your_public_key_file> >> ~/.ssh/authorized_keys
```
 
To run all benchmarks, run the benchmark task and pass in a list of hosts.

For example:
```
./gradlew benchmark -Phosts=localhost,localhost,localhost,localhost -PoutputDir=/tmp/results
```

### Running in aws

This project includes some scripts to automate running benchmarks in AWS. See the 
[README.md](infrastructure/aws/README.md) in the infrastructure/aws directory.

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
public class PartitionedPutBenchmark implements PerformanceTest {

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this::configure);
  }
  
  /**
  * Declare the configuration of the test by calling methods
  * on TestConfig.
  */
  public TestConfig configure() {
    TestConfig testConfig = new TestConfig();
    int locatorPort = 10334;

    //This test has three roles, a geode locator, server, and client
    config.role("locator", 1);
    config.role("server", 2);
    config.role("client", 1);
    
    //Define how the locator,server and client are initialized
    config.before(new StartLocator(locatorPort), "locator");
    config.before(new StartServer(locatorPort), "server");
    config.before(new StartClient(locatorPort), "client");
    config.before(new CreatePartitionedRegion(), "server");
    config.before(new CreateClientProxyRegion(), "client");
    //Define the benchmarked workload, which runs in a client
    config.workload(new PutTask());
    
    return config;
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
  private Region<Object, Object> region;
  
  @Override
  public void setUp(BenchmarkConfiguration cfg) throws Exception {
    super.setUp(cfg);
    ClientCache cache = ClientCacheFactory.getAnyInstance();
    region = cache.getRegion("region");
  }

  
  @Override
  public boolean test(Map<Object, Object> ctx) throws Exception {
    region.put(1,2);
    return true;
  }
}
```
