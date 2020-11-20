package com.app.akka.aot.actor;

import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import com.app.akka.aot.util.Status;
import lombok.Data;

@Data
public class AOTResultData implements AOTicketResult{
    private String ticketID;
    private Stage stage;
    private String reason;
    private Status status;

    public AOTResultData(String ticketID, Stage stage, String reason, Status status) {
        this.ticketID = ticketID;
        this.stage = stage;
        this.reason = reason;
        this.status = status;
    }
}
