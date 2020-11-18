package com.app.akka.aot.actor;

import akka.actor.typed.ActorRef;
import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AOTicketActorHandler implements Command{
    private ClientInputData clientInputData;
    private String ticketID;
    private Stage stage;

    public final ActorRef<AOTicketResult> replyTo;

    public AOTicketActorHandler(ClientInputData clientInputData,
                                String ticketID,
                                ActorRef<AOTicketResult> replyTo,
                                Stage stage) {
        this.clientInputData = clientInputData;
        this.ticketID = ticketID;
        this.replyTo = replyTo;
    }
}
