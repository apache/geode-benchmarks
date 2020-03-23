package org.apache.geode.benchmark.tests;

import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.CLIENT;
import static org.apache.geode.benchmark.topology.ClientServerTopology.Roles.SERVER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import benchmark.geode.data.Session;
import org.junit.jupiter.api.Test;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;

import org.apache.geode.benchmark.tasks.CreatePartitionedRegion;
import org.apache.geode.benchmark.topology.ClientServerTopology;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.PoolFactory;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.perftest.PerformanceTest;
import org.apache.geode.perftest.Task;
import org.apache.geode.perftest.TestConfig;
import org.apache.geode.perftest.TestContext;
import org.apache.geode.perftest.TestRunners;

public class PartitionedWithDeltaAndUniqueObjectReferenceBenchmark implements PerformanceTest {

  private static final String SUBSCRIPTION_POOL = "subscriptionPool";
  private static final String ID = UUID.randomUUID().toString();

  public PartitionedWithDeltaAndUniqueObjectReferenceBenchmark() {}

  @Test
  public void run() throws Exception {
    TestRunners.defaultRunner().runTest(this);
  }

  @Override
  public TestConfig configure() {
    TestConfig config = GeodeBenchmark.createConfig();
    config.threads(180);
    config.durationSeconds(300);
    config.warmupSeconds(30);
    ClientServerTopology.configure(config);
    config.before(new CreatePartitionedRegion(), SERVER);
    config.before(new CreateClientPool(), CLIENT);
    config.before(new CreateClientProxyRegionWithPool(), CLIENT);
    config.workload(new PutWithDeltaTask(ID), CLIENT);
    return config;
  }

  public static class PutWithDeltaTask extends BenchmarkDriverAdapter implements Serializable {

    private Region<String, Session> region;
    private final AtomicReference<String> sessionId = new AtomicReference<>(null);

    private final String id;

    private final List<String> existingSessionAttributeNames =
        new ArrayList<>();

    private final Random random = new Random(System.currentTimeMillis());

    private Collection<Callable<Integer>> workloadTasks;

    public PutWithDeltaTask(String id) {
      this.id = id;
    }

    @Override
    public void setUp(BenchmarkConfiguration cfg) throws Exception {
      super.setUp(cfg);
      this.workloadTasks = newSessionWorkloadTasks();
      ClientCache cache = ClientCacheFactory.getAnyInstance();
      cache.setCopyOnRead(true);
      region = cache.getRegion("region");
      save(Session.create(id));
      this.sessionId.set(id);
    }

    @Override
    public boolean test(Map<Object, Object> ctx) throws Exception {
      int sessionAttributeCount = runSessionWorkload();

      assert sessionAttributeCount == this.existingSessionAttributeNames.size();
      Session session = findById(this.sessionId.get());

      return true;
    }

    private Session findById(String id) {
      return Optional.ofNullable(this.region.get(id))
          .map(Session::commit)
          .map(Session::touch)
          .orElseThrow(() -> new IllegalStateException(
              String.format("No Session with ID [%s] was found", id)));
    }

    private Session save(Session session) {
      if (session != null && session.hasDelta()) {
        this.region.put(session.getId(), session);
        session.commit();
      }

      return session;
    }

    private int runSessionWorkload() {
      return workloadTasks.stream().mapToInt(integerCallable -> {
        try {
          return integerCallable.call();
        } catch (Exception e) {
          e.printStackTrace();
        }
        return 0;
      }).sum();
    }

    private Collection<Callable<Integer>> newSessionWorkloadTasks() {

      Collection<Callable<Integer>> sessionWorkloadTasks = new LinkedList<>();

      sessionWorkloadTasks.add(newAddSessionAttributeTask());
      sessionWorkloadTasks.add(newRemoveSessionAttributeTask());
      sessionWorkloadTasks.add(newSessionReaderTask());
      return sessionWorkloadTasks;
    }

    private Callable<Integer> newAddSessionAttributeTask() {
      return () -> {
        Session session = findById(this.sessionId.get());

        String name = UUID.randomUUID().toString();

        session.setAttribute(name, System.currentTimeMillis());
        save(session);

        this.existingSessionAttributeNames.add(name);
        return 1;
      };
    }

    private Callable<Integer> newRemoveSessionAttributeTask() {
      return () -> {
        int returnValue = 0;

        Session session = findById(this.sessionId.get());
        String attributeName = null;

        int size = this.existingSessionAttributeNames.size();
        if (size > 0) {
          int index = this.random.nextInt(size);
          attributeName = this.existingSessionAttributeNames.remove(index);
        }

        if (session.getAttributeNames().contains(attributeName)) {
          session.removeAttribute(attributeName);
          returnValue = -1;
        } else {
          Optional.ofNullable(attributeName)
              .filter(it -> !it.trim().isEmpty())
              .ifPresent(this.existingSessionAttributeNames::add);
        }

        save(session);

        return returnValue;
      };
    }

    private Callable<Integer> newSessionReaderTask() {
      return () -> {
        Session session = findById(this.sessionId.get());
        save(session.touch());

        return 0;
      };
    }

    private int safeFutureGet(Future<Integer> future) {
      try {
        return future.get();
      } catch (Exception cause) {
        throw new RuntimeException("Session access task failure", cause);
      }
    }
  }

  private class CreateClientPool implements Task {

    @Override
    public void run(TestContext context) throws Exception {
      PoolFactory poolFactory = PoolManager.createFactory();
      poolFactory
          .addLocator("localhost", 10334)
          .setMaxConnections(-1)
          .setPingInterval(1000)
          .setRetryAttempts(5)
          .setSubscriptionEnabled(true)
          .create(SUBSCRIPTION_POOL);
    }
  }

  private class CreateClientProxyRegionWithPool implements Task {

    @Override
    public void run(TestContext context) throws Exception {
      ClientCache clientCache = (ClientCache) context.getAttribute("CLIENT_CACHE");
      clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY)
          .setPoolName(SUBSCRIPTION_POOL)
          .create("region");
    }
  }
}
