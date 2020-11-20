package com.app.akka.aot.actor;

import akka.actor.typed.ActorRef;
import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AOTTicketFailureHandler  implements  Command{
    private String ticketID;
    private Stage stage;
    private String reason;

    public final ActorRef<AOTResultData> replyTo;

    public AOTTicketFailureHandler(String ticketID, Stage stage, ActorRef<AOTResultData> replyTo, String reason) {
        this.ticketID = ticketID;
        this.stage = stage;
        this.replyTo = replyTo;
        this.reason = reason;
    }
}
