# Benchmark Utilities for AWS

These utilities create instances and run tests in your AWS account

# Prerequisites
* You must have the aws cli installed.
* You must also set your secret key for the CLI. You must set up a profile named `geode-benchmarks`, so use the command `aws configure --profile geode-benchmarks` to configure the CLI. See [Amazon's instructions](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html)
* To build the image, you must have packer installed

# Image

Before using the scripts below, build the image in the image directory using the `build_image.sh` script.

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

    run_test.sh -t [tag] [-v [version] | -b [branch]] <options...>

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
    
    run_test.sh -t [tag] [-v [version] | -b [branch]] [-V [baseline version] | -B [baseline branch]] <options...>"

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
./run-tests --tag mycluster --geode-branch develop --benchmark-branch benchmarkBranch --metadata "'name':'HelenaTestingCPUs','CPU':'256','geodeBranch':'CPUTest'"
./destroy_cluster.sh --tag mycluster
```