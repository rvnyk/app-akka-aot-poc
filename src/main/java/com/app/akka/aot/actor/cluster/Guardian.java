package com.app.akka.aot.actor.cluster;

import akka.actor.AddressFromURIString;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import com.app.akka.aot.actor.memory.ClientAOTicket;
import com.app.akka.aot.actor.persistent.ClientAOTicketPersistent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Root actor bootstrapping the application
 */
@Component
@Getter
public class Guardian {

  private ActorSystem system;
  private ClusterSharding sharding;
  private Duration timeout;

  public ClusterSharding getSharding() {
    return sharding;
  }

  public Duration getTimeout() {
    return timeout;
  }

  //@Bean
  public  Behavior<Void> createsystemBehaviour() {
    System.out.println("createBehaviour() called");
    return Behaviors.setup(context -> {
      //ClientAOTicket.initSharding(context.getSystem());
      ClientAOTicketPersistent.initSharding(context.getSystem());
      this.system = context.getSystem();
      sharding = ClusterSharding.get(system);
      timeout = system.settings().config().getDuration("akka.cluster.auto-down-unreachable-after");
      return Behaviors.empty();
    });
  }

  @PostConstruct
  public void createActorSystem(){
    Behavior<Void> behavior = createsystemBehaviour();
    System.out.println("createActorSystem() called");
    System.out.println("behavior ->  " + behavior);
    List<Integer> seedNodePorts =
            ConfigFactory.load().getStringList("akka.cluster.seed-nodes")
                    .stream()
                    .map(AddressFromURIString::parse)
                    .map(addr -> (Integer) addr.port().get()) // Missing Java getter for port, fixed in Akka 2.6.2
                    .collect(Collectors.toList());

    // Either use a single port provided by the user
    // Or start each listed seed nodes port plus one node on a random port in this single JVM if the user
    // didn't provide args for the app
    // In a production application you wouldn't start multiple ActorSystem instances in the
    // same JVM, here we do it to simplify running a sample cluster from a single main method.
    String []args = {};
    List<Integer> ports = Arrays.stream(args).findFirst().map(str ->
            Collections.singletonList(Integer.parseInt(str))
    ).orElseGet(() -> {
      List<Integer> portsAndZero = new ArrayList<>(seedNodePorts);
      portsAndZero.add(0);
      return portsAndZero;
    });

    for (int port : ports) {
      final int httpPort;
      if (port > 0) httpPort = 10000 + port;  // offset from akka port
      else httpPort = 0; // let OS decide
       ActorSystem.create(behavior, "AOTAkkaCluster", configWithPort(port));
    }

  }

  public Config configWithPort(int port) {

    return ConfigFactory.parseMap(
            Collections.singletonMap("akka.remote.artery.canonical.port", Integer.toString(port))
    ).withFallback(ConfigFactory.load());
  }
}
