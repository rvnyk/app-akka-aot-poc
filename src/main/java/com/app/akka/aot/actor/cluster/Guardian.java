package com.app.akka.aot.actor.cluster;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.app.akka.aot.actor.ClientAOTicket;

/**
 * Root actor bootstrapping the application
 */
final class Guardian {

  public static Behavior<Void> create() {
    return Behaviors.setup(context -> {
      ClientAOTicket.initSharding(context.getSystem());

      return Behaviors.empty();
    });
  }
}
