## Minimum set of information a test needs to provide
 - Tuneable parameters under test control (may be derived from tasks)
    - Including minimal defaults to run as a functional test in IDE?
    - Including preferred scale to run at?
 - Required member roles, and how they should be mapped to physical hosts
 - Initialization tasks for roles, and any dependencies between initialization steps. No teardown - env will be cleaned by framework
 - Workload tasks for roles, and their desired mix


## Framework needs to 
Combine
 - a Test or list of tests
 - an infrastructure provisioner (which may have it's own parameters)
 - Scaling parameters ?? <- Or should this be part of the test?
 - Credentials for infrastructure, other tunables 
 
And produce
 - Statistics for each test, as well as product logs and stats

Post run tools need to be able to
 - Compare stats between runs and pass fail
 - Chart stats