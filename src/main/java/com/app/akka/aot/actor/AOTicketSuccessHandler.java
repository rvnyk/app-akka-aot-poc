package com.app.akka.aot.actor;

import akka.actor.typed.ActorRef;
import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AOTicketSuccessHandler implements Command{
    private String ticketID;
    private Stage stage;
    private String reason;
    private ClientInputData clientInputData;
    public final ActorRef<AOTResultData> replyTo;
}
