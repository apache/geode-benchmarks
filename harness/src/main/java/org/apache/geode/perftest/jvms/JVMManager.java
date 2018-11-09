package org.apache.geode.perftest.jvms;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.geode.perftest.infrastructure.CommandResult;
import org.apache.geode.perftest.infrastructure.Infrastructure;
import org.apache.geode.perftest.jvms.classpath.ClassPathCopier;
import org.apache.geode.perftest.jvms.rmi.ChildJVM;
import org.apache.geode.perftest.jvms.rmi.Controller;

/**
 * Manager for launching JVMs and a given infrastructure and setting up RMI
 * access to all JVMs.
 */
public class JVMManager {

  public static final String RMI_HOST = "RMI_HOST";
  public static final String RMI_PORT = "RMI_PORT";
  public static final String CONTROLLER = "CONTROLLER";
  public static final String JVM_ID = "JVM_ID";

  /**
   * Start all requested JVMs on the infrastructure
   * @param infra The infrastructure to use
   * @param roles The JVMs to start. Keys a roles and values are the number
   * of JVMs in that role.
   *
   * @return a {@link RemoteJVMs} object used to access the JVMs through RMI
   */
  public RemoteJVMs launch(Infrastructure infra,
                           Map<String, Integer> roles) throws Exception {

    Set<Infrastructure.Node> nodes = infra.getNodes();
    int numWorkers = roles.values().stream().mapToInt(Integer::intValue).sum();

    if(nodes.size() < numWorkers) {
      throw new IllegalStateException("Too few nodes for test. Need " + numWorkers + ", have " + nodes.size());
    }

    int rmiPort = 33333;
    Registry registry = LocateRegistry.createRegistry(rmiPort);

    CountDownLatch workersStarted = new CountDownLatch(numWorkers);

    Controller controller = new Controller(worker -> workersStarted.countDown());
    registry.bind(CONTROLLER, controller);

    List<JVMMapping> mapping = mapRolesToNodes(roles, nodes);

    String classpath = System.getProperty("java.class.path");
    ClassPathCopier copier = new ClassPathCopier(classpath);
//    copier.copyToNodes(infra);

    for(JVMMapping entry : mapping) {
      String[] shellCommand = buildCommand(InetAddress.getLocalHost().getHostAddress(), rmiPort, entry.getId());
      CompletableFuture<CommandResult> result = CompletableFuture.supplyAsync(() -> {
        try {
          return infra.onNode(entry.node, shellCommand);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      result.thenAccept(commandResult -> {
        System.err.println("ChildJVM exited with code " + commandResult.getExitStatus() + ", output\n" + commandResult.getOutput());
      });
    }

    if(!workersStarted.await(1, TimeUnit.MINUTES)) {
      throw new IllegalStateException("Workers failed to start in 1 minute");
    }

    return new RemoteJVMs(mapping, controller);
  }

  private String[] buildCommand(String rmiHost, int rmiPort, int id) {

    List<String> command = new ArrayList<String>();
    command.add("java");
    command.add("-classpath");
    command.add("lib/*");
    command.add("-D" + RMI_HOST + "=" + rmiHost);
    command.add("-D" + RMI_PORT + "=" + rmiPort);
    command.add("-D" + JVM_ID + "=" + id);
    command.add(ChildJVM.class.getName());

    return command.toArray(new String[0]);
  }

  private List<JVMMapping> mapRolesToNodes(Map<String, Integer> roles,
                                           Set<Infrastructure.Node> nodes) {


    List<JVMMapping> mapping = new ArrayList<>();
    Iterator<Infrastructure.Node> nodeItr = nodes.iterator();

    int id = 0;
    for(Map.Entry<String, Integer> roleEntry : roles.entrySet()) {
      for(int i = 0; i < roleEntry.getValue(); i++) {
        Infrastructure.Node node = nodeItr.next();
        mapping.add(new JVMMapping(node, roleEntry.getKey(), id++));
      }

    }
    return mapping;
  }

  public static class JVMMapping implements Serializable {
    private final Infrastructure.Node node;
    private final String role;
    private final int id;

    public JVMMapping(Infrastructure.Node node, String role, int id) {
      this.node = node;
      this.role = role;
      this.id = id;
    }

    public Infrastructure.Node getNode() {
      return node;
    }

    public String getRole() {
      return role;
    }

    public int getId() {
      return id;
    }

  }
}
