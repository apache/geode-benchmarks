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
Runs benchmark tests against a single branch of `geode`. Arguments are (in order)

* tag (the same as the cluster launched via `launch_cluster.sh`)
* branch of geode (must exist in the apache geode repository)
* branch of benchmark code to use (must exist in the apache geode-benchmarks repository)
* (Optional) output directory for results

# run_against_baseline.sh
Runs benchmark tests against two branches of geode for comparison purposes. Arguments are (in order)

* tag (the same as the cluster launched via `launch_cluster.sh`)
* branch of geode (must exist in the apache geode repository)
* branch of benchmark code to use (must exist in the apache geode-benchmarks repository)


# destroy_cluster.sh
Destroys a cluster that you created. Arguments are the tag that you passed to launch_cluster.sh

#Example
```bash
./launch_cluster.sh mycluster 4
./run-tests mycluster
./destroy_cluster.sh
```
