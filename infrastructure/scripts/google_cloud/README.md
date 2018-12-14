# Benchmark Utilities for Google Cloud Platform


These utilities create instances and run tests in your google cloud account. 

# Prerequisites
* You must have the [google cloud sdk](https://cloud.google.com/sdk/) installed.
* You must also set a default region and zone via the following commands:
```bash
gcloud config set project <project>
gcloud config set compute/region <region>
gcloud config set compute/zone <zone>

```

otherwise the scripts may prompt you for these values.

# build_image.sh
`build_image.sh` creates a compute image suitable for launching benchmarks on. It takes no 
arguments. Run this before running any other scripts if you are running benchmarks in a GCP project 
you've never run benchmarks in before.

# launch_cluster.sh
`launch_cluster.sh` creates an instance group in GCP based on an image created by `build_image.sh`.

It takes three arguments. First, a tag to identify the cluster for use with other utilities. The
second argument is the number of instances to create. The third and final argument is the subnet to
create the instances in. In general this should be specified as 
`projects/<gcp-project>/regions/<region>/subnetworks/<subnet-name>`

# run_tests.sh
Runs benchmark tests against a single branch of `geode`. Arguments are (in order)

* tag (the same as the cluster launched via `launch_cluster.sh`)
* branch of geode (must exist in the apache geode repository)
* output directory for results
* branch of benchmark code to use (must exist in the apache geode-benchmarks repository)

# run_against_baseline.sh
Runs benchmark tests against two branches of geode for comparison purposes. Arguments are (in order)

* tag (the same as the cluster launched via `launch_cluster.sh`)
* branch of geode (must exist in the apache geode repository)
* branch of benchmark code to use (must exist in the apache geode-benchmarks repository)


# destroy_cluster.sh
Destroys a cluster that you created. Arguments are the tag that you passed to launch_cluster.sh

#Example
```bash
./build_image.sh
./launch_cluster.sh mycluster 4
./run-tests mycluster
./destroy_cluster.sh
```