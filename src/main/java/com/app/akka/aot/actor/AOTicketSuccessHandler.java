package com.app.akka.aot.actor;

import akka.actor.typed.ActorRef;
import com.app.akka.aot.util.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AOTicketSuccessHandler implements Command{
    private String ticketID;
    private Stage stage;
    private String reason;
    public final ActorRef<AOTResultData> replyTo;
}
