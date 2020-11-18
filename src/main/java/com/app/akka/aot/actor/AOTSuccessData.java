package com.app.akka.aot.actor;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AOTSuccessData implements AOTicketResult{
    private String ticketID;

    public AOTSuccessData(String ticketID) {
        this.ticketID = ticketID;
    }
}
