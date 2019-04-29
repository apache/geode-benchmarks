# Benchmark Utilities for AWS

These utilities create instances and run tests in your AWS account

# Prerequisites
* You must have the aws cli installed. If `aws` is not on your path then you can try to install it with `pip3 install awscli --upgrade --user`. See [Amazon's aws cli installation instructions](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)
* You must also set your secret key for the CLI. You must set up a profile named `geode-benchmarks`, so use the command `aws configure --profile geode-benchmarks` to configure the CLI. You will need to specify the "AWS Access Key ID" and "AWS Secret Access Key". You can get these from an existing team member. Set the "Default region name" to "us-west-2". See [Amazon's instructions](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html)

# Image

If you need to build the image, you must have packer installed. But you can run the following scripts (launch, run, destroy) without building the image.
Build the image in the image directory using the `build_image.sh` script.

# launch_cluster.sh
Creates an instance group in AWS based on an image created.

Usage:

    ./launch_cluster.sh -t [tag] -c [count] [--ci]

Options:
    
    -t|--tag     : Cluster tag to identify the cluster for use with other utilities
    -c|--count   : Number of AWS instances to start (recommended: 4)
    --ci         : (Optional) Set when the instances are being started for use in Continuous Integration
    -h|-?|--help : Help message

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
    -h|-?|--help                                         : Help message


# destroy_cluster.sh
Destroys a cluster that you created.

Usage:

    ./destroy_cluster.sh -t [tag] [--ci]
    
Options:
    
    -t|--tag     : Cluster tag to identify the cluster for use with other utilities
    --ci         : (Optional) Set when the instances are being started for use in Continuous Integration
    -h|-?|--help : Help message


#Example

Example 1 - run_test.sh:
```bash
./launch_cluster.sh --tag mycluster --count 4
./run_tests.sh --tag mycluster --geode-branch develop --benchmark-branch benchmarkBranch --metadata "'name':'HelenaTestingCPUs','CPU':'256','geodeBranch':'CPUTest'"
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
./launch_cluster --tag profiling --count 4
./copy_to_cluster.sh --tag profiling -- /Applications/YourKit-Java-Profiler-2019.1.app/Contents/Resources/bin/linux-x86-64/libyjpagent.so .
./run_tests.sh --tag profiling --geode-branch develop -- -i -Pbenchmark.profiler.argument=-agentpath:/home/geode/libyjpagent.so=disablestacktelemetry,exceptions=disable,delay=60000,sessionname=JVM_ROLE-JVM_ID
./destroy_cluster.sh --tag profiling
```