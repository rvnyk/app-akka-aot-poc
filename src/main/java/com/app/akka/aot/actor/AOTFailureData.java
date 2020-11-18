package com.app.akka.aot.actor;

import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import lombok.Data;

@Data
public class AOTFailureData implements AOTicketResult{
    private String ticketID;
    private Stage stage;
    private String reason;

    public AOTFailureData(String ticketID, Stage stage, String reason) {
        this.ticketID = ticketID;
        this.stage = stage;
        this.reason = reason;
    }
}
