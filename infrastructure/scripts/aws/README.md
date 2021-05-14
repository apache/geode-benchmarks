# Benchmark Utilities for AWS

These utilities create instances and run tests in your AWS account

# Prerequisites
* You must have the aws cli installed. If `aws` is not on your path then you can try to install it with `pip3 install awscli --upgrade --user`. See [Amazon's aws cli installation instructions](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)
* You must also set your secret key for the CLI. You must set up a profile named `geode-benchmarks`, so use the command `aws configure --profile geode-benchmarks` to configure the CLI. You will need to specify the "AWS Access Key ID" and "AWS Secret Access Key," which can be obtained from a team member. Set the "Default region name" to "us-west-2". See [Amazon's instructions](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html)

	Alternately, if you already have AWS credentials and just need to create the `geode-benchmarks` profile, you can first modify your existing AWS credentials file (found at `~/.aws/credentials`) and add the following lines:

        [geode-benchmarks]
        aws_access_key_id = Your access ID key
        aws_secret_access_key = Your secret access key

	Then modify the `config` file found in the same directory and add the following:

        [profile geode-benchmarks]
        region = us-west-2


# Image

If you need to build the image, you must have packer installed. The following scripts (launch, run, analyze, destroy) can be run without building the image.
Build the image in the image directory using the `build_image.sh` script.


# Using Environment Variables For configuration

In order to have the scripts know which configuration data to use, the data must be provided.
One of the ways that can be done is via environment variables.

    prompt> aws configure
    AWS Access Key ID [****************foo1]:
    AWS Secret Access Key [****************bar2]:
    Default region name [us-west-2]:

Export environment variables as follows.

    export AWS_ACCESS_KEY_ID=myaccesskeyfoo1
    export AWS_SECRET_ACCESS_KEY=mysecretaccesskeybar2
    export AWS_REGION="us-west-2"


# launch_cluster.sh
Creates an instance group in AWS based on an image created.

Usage:

    ./launch_cluster.sh -t [tag] -c [count]

Options:

    -t|--tag             : Cluster tag to identify the cluster for use with other utilities
    -c|--count           : Number of AWS instances to start (recommended: 4)
    -i|--instance-type   : AWS instance type to start (default: c5.18xlarge)
    --tenancy            : AWS tenancy. (default: host)
    --availability-zone  : AWS availability zone. (default: us-west-2a)
    --ci                 : (Optional) Set when the instances are being started for use in Continuous Integration
    -h|-?|--help         : Help message

# run_tests.sh
Runs benchmark tests against a single branch of `geode` on the AWS instances with the specified tag.

Usage:

    run_tests.sh -t [tag] [-v [version] | -b [branch]] <options...>

Options:

    -t|--tag                        : Cluster tag
    -p|--br|--benchmark-repo        : Benchmark repo (default: apache/geode-benchmarks)
    -e|--bb|--benchmark-branch      : Benchmark branch (optional - defaults to develop)
    -o|--output                     : Output directory (optional - defaults to ./output-<date>-<tag>)
    -v|--version|--geode-version    : Geode Version
    -r|--gr|--geode-repo            : Geode repo (default: apache/geode)
    -b|--gb|--branch|--geode-branch : Geode Branch (default: develop)
    -m|--metadata                   : Test metadata to output to file, comma-delimited (optional)
    --                              : All subsequent arguments are passed to the benchmark tast as arguments
    -h|-?|--help                    : Help message

# run_against_baseline.sh
Runs benchmark tests against two branches of geode for comparison purposes on the AWS instances with
the specified tag.

Usage: 
    
    run_against_baseline.sh -t [tag] [-v [version] | -b [branch]] [-V [baseline version] | -B [baseline branch]] <options...>"

Options:
           
    -t|--tag                                             : Cluster tag
    -p|--br|--benchmark-repo                             : Benchmark repo (default: apache/geode-benchmarks) 
    -e|--bb|--benchmark-branch                           : Benchmark branch (optional - defaults to develop)
    -o|--output                                          : Output directory (optional - defaults to ./output-<date>-<tag>)
    -v|--version|--geode-version                         : Geode Version
    -r|--gr|--repo|--geode-repo                          : Geode repo (default: apache/geode)
    -b|--gb|--branch|--geode-branch                      : Geode Branch (default: develop)
    -R|--bgr|--baseline-repo|--baseline-geode-repo       : Geode Baseline Repo (default: apache/geode)
    -V|--bgv|--baseline-version|--baseline-geode-version : Geode Baseline Version
    -B|--gbb|--baseline-branch|--baseline-geode-branch   : Geode Baseline Branch (default: develop)
    -m|--metadata                                        : Test metadata to output to file, comma-delimited (optional)
    --ci                                                 : Set when the instances are being started for use in Continuous Integration
    --                                                   : All subsequent arguments are passed to the benchmark task as arguments
    -h|-?|--help                                         : Help message

    e.g. ./run_against_baseline.sh -t test_environment  -v <sha1 of target version> -V <sha1 of base version>  -R <baseline repo e.g. user/geode> -B <baseline branch name> -b <target branch name> -r <target repo e.g. user/geode>


# analyze_tests.sh
Compares the results of two benchmark runs and outputs analysis of their relative performance.

Usage:

    ./analyze_tests.sh  [-o <output directory> | [--baselineDir <baseline directory> --branchDir <branch directory>]] [options ...] [-- arguments ...]

Options:

    -o|--output|--outputDir  : The directory containing benchmark results
    --baseline|--baselineDir : The directory containing baseline benchmark results
    --branch|--branchDir     : The directory containing branch benchmark results
    --ci                     : Set if starting instances for Continuous Integration
    --                       : All subsequent arguments are passed to the benchmark task as arguments.
    -h|--help                : This help message


# destroy_cluster.sh
Destroys a cluster that you created.

Usage:

    ./destroy_cluster.sh -t [tag] [--ci]
    
Options:
    
    -t|--tag     : Cluster tag to identify the cluster for use with other utilities
    --ci         : (Optional) Set when the instances are being started for use in Continuous Integration
    -h|-?|--help : Help message


## Example

Example 1 - Generating and comparing two benchmark runs using run_tests.sh and analyze_tests.sh. 

The first command creates a new cluster with 4 instances and the tag "mycluster" using launch_cluster.sh.

The second command runs only the `MyCustomBenchmark` benchmark test (by using the `-- --tests=MyCustomBenchmark` argument) found on the `myBenchmarkBranch` branch of the `myGit/geode-benchmarks` repository. This benchmark runs against the develop branch of Geode, adds some metadata, and outputs the results to `~/benchmarking/baseline` using run_test.sh. 

The third command runs the same benchmark against the `myGeodeBranch` branch of the `myGit/geode` repository and outputs the results to `~/benchmarking/branch` using run_tests.sh. 

The fourth command compares the results found in `~/benchmarking/branch` to the results found in `~/benchmarking/baseline` and outputs analysis of the operations per second and latency of the branch benchmark relative to the baseline using analyze_tests.sh.

The fifth command destroys the cluster using destroy_cluster.sh.
```bash
./launch_cluster.sh --tag mycluster --count 4
./run_tests.sh --tag mycluster --geode-branch develop --benchmark-repo myGit/geode-benchmarks --benchmark-branch myBenchmarkBranch --metadata "'name':'HelenaTestingCPUs','CPU':'256','geodeBranch':'CPUTest'" --output ~/benchmarking/baseline -- --tests=MyCustomBenchmark
./run_tests.sh --tag mycluster --geode-repo myGit/geode --geode-branch myGeodeBranch --benchmark-repo myGit/geode-benchmarks --benchmark-branch myBenchmarkBranch --metadata "'name':'HelenaTestingCPUs','CPU':'256','geodeBranch':'CPUTest'" --output ~/benchmarking/branch -- --tests=MyCustomBenchmark
./analyze_tests.sh --branch ~/benchmarking/branch --baseline ~/benchmarking/baseline
./destroy_cluster.sh --tag mycluster
```

Example 2 - run_against_baseline.sh:
```bash
./launch_cluster.sh --tag mycluster --count 4
./run_against_baseline.sh --tag mycluster --geode-branch develop --benchmark-branch benchmarkBranch --metadata "'name':'HelenaTestingCPUs','CPU':'256','geodeBranch':'CPUTest'"
./destroy_cluster.sh --tag mycluster
```

# Running with Profiler

## Prerequisites
* You must have fulfilled the prerequisites at the beginning of this doc
* You must have YourKit installed
* Launch YourKit
* In the top menu bar, select `Filters...` under the `Settings` dropdown
  * Uncheck the filter with the name  `org.apache`
  * Click the `Ok` button to save the configuration
    * About this setting: When this box is checked, classes in the `org.apache` namespace will not 
    be profiled, so it must be unchecked to profile geode or geode-benchmarks
* Quit YourKit
* Use your chosen text editor to edit your `/Users/<yourUsername>/.yjp/ui.ini` and add 
`-Dyjp.zero.time.methods=false`. Restart the profiler for this change to take effect. 
  * About this setting: When this value is set to true, Unsafe Park and Unsafe Wait are ignored, 
  which results in Reentrant Locks not appearing in the profiler. Setting this value to false will 
  result in some noise from pools that are waiting for work, but is also necessary in order to see 
  contention around reentrant locks.

## Running in AWS
* Launch YourKit
* On the YourKit "Welcome" page, under the "Monitor Applications" section, hit the green plus to add
a configuration
* In the configuration window, fill in the following:
  * `Connection name`: some name for the configuration (the name of the member that is being 
  connected is usually a good choice)
  * `Host or IP Address`: the public IP of the AWS VM hosting the member with which you want to 
  connect (the launch cluster script prints these in the order that they were started: 
  [locator-0, server-1, server-2, client-3])
  * Select `Advanced` and fill in the following:
    * `SSH User`: `geode`
    * `SSH port`: `22`
    * Click on "Authentication Settings..." and on that window, fill in the following:
      * `Authentication method`: `private key`
      * `Private Key`: `/Users/<yourUsername>/.geode-benchmarks/<clusterTag>-privkey.pem`
      * `Passphrase`: leave blank
* Once you have saved the configuration, it should show up under the "Monitor Applications" section 
of the page with the connection status. If the machines are running, the status should be "No 
applications running". If there is a test in progress, you should be able to click through to 
monitor the test.
* Pull up a terminal and navigate to `geode-benchmarks/infrastructure/scripts/aws`
* Copy the YourKit file to the AWS VMs using the following command
  * `./copy_to_cluster.sh -tag <clusterTag> -- <path to libyjpagent.so> <destination path>`
  * Your path to the `libyjpagent.so` is probably `/Applications/YourKit-Java-Profiler-2019.1.app/Contents/Resources/bin/linux-x86-64/libyjpagent.so`
  * Your destination path should probably be `.`, which will end up putting the file at `/home/geode/` 
* Run the test using the `run_tests.sh` script, with the additional CLI option `-Pbenchmark.profiler.argument`:
  * `./run_tests.sh --tag <clusterTag> [other CLI options] -- -i -Pbenchmark.profiler.argument=-agentpath:/home/geode/libyjpagent.so=disablestacktelemetry,exceptions=disable,delay=60000,sessionname=JVM_ROLE-JVM_ID`
* Return to YourKit and profile as usual
  
## Example
```bash
./launch_cluster.sh --tag profiling --count 4
./copy_to_cluster.sh --tag profiling -- /Applications/YourKit-Java-Profiler-2019.1.app/Contents/Resources/bin/linux-x86-64/libyjpagent.so .
./run_tests.sh --tag profiling --geode-branch develop -- -i -Pbenchmark.profiler.argument=-agentpath:/home/geode/libyjpagent.so=disablestacktelemetry,exceptions=disable,delay=60000,sessionname=JVM_ROLE-JVM_ID
./destroy_cluster.sh --tag profiling
```

# Running with SSL enabled
To run benchmarks with SSL enabled, run the test using the `run_tests.sh` script, with the additional CLI option `-PwithSsl`:
```
./run_tests.sh --tag <clusterTag> [other CLI options] -- -PwithSsl
```
