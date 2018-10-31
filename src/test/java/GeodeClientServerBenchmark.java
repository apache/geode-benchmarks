import infrastructure.Infrastructure;
import test.CloudTest;
import test.TestConfig;
import test.TestContext;

public abstract class GeodeClientServerBenchmark implements CloudTest {
  @Override
  public TestConfig configure(Infrastructure infrastructure) {
//    return new TestConfig().workload(this::doOperation);
    return null;
  }

  public abstract void doOperation(TestContext context);
}
