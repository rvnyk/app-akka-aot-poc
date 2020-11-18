package com.app.akka.aot.actor;

import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AOTicketSuccessHandler implements Command{
    private String ticketID;
    public final ActorRef<AOTicketResult> replyTo;
}
