Infrastructure layer

Responsibilities
- Provisions hardware
- Launches actual test runner on a hardware host, passing it a list of provisioned host names (all the same?)
- Note - the number of hosts will be passed in by the user ... not read from the test? It is the tests responsibility to make the best use of the hardware, or the user's responsibility to provision a reasonable amount of hardware.
- Config params for this layer are - keys to cloud compute if necessary, number of hosts if necessary. Should set up passwordless ssh between all hosts. Maybe host specs?
- What to have on host - machine image
- Tears down hardware and copies results to users machine


Suite running layer
- Given a suite of tests, run all of them and generate a separate result dir for each
- params are the list of tests to run and the hosts to run them on
- Can we use junit for this, maybe? Or not?
- In hydra, and yardstick, a test seems to consist of a Java class and some configuration parameters for that class...

Test launcher
- Launch JVMs for tests using ssh to nodes
- Setup RMI between JVMs (maybe? Or not necessary in yardstick style)
- Execute test
- Gather results
- Tear down JVMs
- params are 
 - the hosts. 
 - The launching command to use for different roles, from the test itself (if we want to use gfsh to launch processes).
 - Classpasth for JVMs, or launching command?
 - The individual test (written in Java)


Test runner 
- Run test with different phases
- Measure performance statistics for workload tasks (percentile latency, throughput and stddev) after warm up phase.
- Generate result dir with statistics on each member at the end of the test (or gather them
- Params are - the test itself, written in java. Probably consists of gemfire configuration, initialization tasks, and workload tasks. The RMI handles for all processes.


Performance testing wrapper
 - Workload tasks should be wrapped in metrics capturing code for later analysis



Performance comparision module
 - Given multiple performance suite results, chart them
 - Given two performance suite results, report deviations greater than x% 


