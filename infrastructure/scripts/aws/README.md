# Benchmark Utilities for AWS

These utilities create instances and run tests in your AWS account

# Prerequisites
* You must have the aws cli installed.
* You must also set your secret key for the CLI. You must set up a proflie named `geode-benchmarks`, so use the command `aws configure --profile geode-benchmarks` to configure the CLI. See [Amazon's instructions](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html)
* To build the image, you must have packer installed

# Image

Before using the scripts below, build the image in the image directory using the `build_image.sh` script.

# launch_cluster.sh
`launch_cluster.sh` creates an instance group in AWS based on an image created.

It takes two arguments. First, a tag to identify the cluster for use with other utilities. The
second argument is the number of instances to create. 

# run_tests.sh
Runs benchmark tests against a single branch of `geode` on the AWS instances with the specified tag.

Usage: 

    run_test.sh -t [tag] [-v [version] | -b [branch]] <options...>

Options:
    
    -e : Benchmark branch (optional - defaults to develop)
    -o : Output directory (optional - defaults to ./output-<date>-<tag>)
    -v : Geode Version
    -b : Geode Branch
    -t : Cluster tag
    -m : Test metadata to output to file, comma-delimited (optional)
    -h : Help message

# run_against_baseline.sh
Runs benchmark tests against two branches of geode for comparison purposes on the AWS instances with
the specified tag.

Usage: 
    
    run_test.sh -t [tag] [-v [version] | -b [branch]] [-V [baseline version] | -B [baseline branch]] <options...>"

Options:
            
    -e : Benchmark branch (optional - defaults to develop)
    -o : Output directory (optional - defaults to ./output-<date>-<tag>)
    -v : Geode Version
    -b : Geode Branch
    -V : Geode Baseline Version
    -B : Geode Baseline Branch
    -t : Cluster tag
    -m : Test metadata to output to file, comma-delimited (optional)
    -h : Help message


# destroy_cluster.sh
Destroys a cluster that you created. Arguments are the tag that you passed to launch_cluster.sh

#Example

Example 1 - run_test.sh:
```bash
./launch_cluster.sh mycluster 4
./run_tests.sh -t mycluster -b develop -e benchmarkBranch -m "'name':'HelenaTestingCPUs','CPU':'256','geodeBranch':'CPUTest'"
./destroy_cluster.sh mycluster
```

Example 2 - run_against_baseline.sh:
```bash
./launch_cluster.sh mycluster 4
./run-tests -t mycluster -b develop -e benchmarkBranch -m "'name':'HelenaTestingCPUs','CPU':'256','geodeBranch':'CPUTest'"
./destroy_cluster.sh mycluster
```