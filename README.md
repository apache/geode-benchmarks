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
The benchmarks require machines with passwordless ssh enabled in order to run, so ensure that the authentication key pair for SSH does not have a passphrase. If you have previously created a key pair with a passphrase, you can create a different key pair which
is of a different type than the previously created one. Be sure to backup your existing key pair before creating a new one. The public key needs to be in PEM format, but some newer OpenSSH
versions default to a new format. PEM format can be forced by using `-m PEM`:
```
ssh-keygen -m PEM -t rsa
```
While runinng a test on a single machine (i.e. localhost) add the generated key to `authorized_keys` to authorize the user:
```
cat <your_public_key_file> >> ~/.ssh/authorized_keys
```
Test if you can ssh to localhost:
```
ssh localhost
```
As long as that works, we are good to go.

Get your local hosts name:
```
hostname
```
Edit /etc/hosts and add the local host name with and without domain to localhost entries.
```
127.0.0.1 localhost mycomputer mycomputer.mydomain
::1       localhost mycomputer mycomputer.mydomain
```
 
To run all benchmarks, run the benchmark task and pass in a list of hosts.

For example:
```
./gradlew benchmark -Phosts=localhost,localhost,localhost,localhost
```

Options:
```
    -Phosts               : Hosts used by benchmarks on the order of client,locator,server,server (-Phosts=localhost,localhost,localhost,localhost)
    -PoutputDir           : Results output directory (-PoutputDir=/tmp/results)
    -PtestJVM             : Path to an alternative JVM for running the client, locator, and servers. If not specified JAVA_HOME will be used. Note all compilation tasks will still use JAVA_HOME.
    -PwithSsl             : Flag to run geode with SSL. A self-signed certificate will be generated at runtime.
    -PwithSecurityManager : Flag to start Geode with the example implementation of SecurityManager
    -PwithGc              : Select which GC to use. Valid values CMS (default), G1, Z.
    -PwithHeap            : Specify how large a heap the benchmark VMs should use, default "8g". Accepts any `-Xmx` value, like "32g".
    --tests               : Specific benchmarks to run (--tests=PartitionedPutBenchmark)
    -d                    : Debug
    -i                    : Info
```    
### Scripts for running in aws and analyzing results

This project includes some scripts to automate running benchmarks in AWS and analyzing the results produced (as well as the results produced from running locally). See the 
[README.md](infrastructure/scripts/aws/README.md) in the infrastructure/aws directory.

## Project structure

The project is divided into two modules
* harness - Contains test framework code for running benchmarks. Does not depend on Apache Geode.
* geode-benchmarks - Individual benchmarks of Apache Geode.

## Sample benchmark

Benchmarks are defined in declarative configuration classes. Each configuration class is run as 
a junit test which calls the configure method and passes it to the TestRunner, which executes
the test on the provided TEST_HOSTS.

Benchmarks are composed of `before` tasks, `after` tasks, and `workload` tasks. Each seperate `before` and `after`
task is run once. `Workload` tasks are run repeatedly and their execution time is measured and
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
