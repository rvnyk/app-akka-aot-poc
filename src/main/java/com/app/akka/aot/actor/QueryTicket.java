package com.app.akka.aot.actor;

import akka.actor.typed.ActorRef;
import lombok.Data;

@Data
public class QueryTicket implements Command{
    private String ticketID;
    public final ActorRef<AOTicketCurrentInfo> replyTo;
}
