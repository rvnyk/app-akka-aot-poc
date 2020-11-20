package com.app.akka.aot.actor;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryTicket implements Command{
    private String ticketID;
    public final ActorRef<AOTicketCurrentInfo> replyTo;

    public QueryTicket(String ticketID, ActorRef<AOTicketCurrentInfo> replyTo) {
        this.ticketID = ticketID;
        this.replyTo = replyTo;
    }
}
