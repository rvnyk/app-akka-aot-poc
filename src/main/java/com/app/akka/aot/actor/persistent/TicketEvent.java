package com.app.akka.aot.actor.persistent;

import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@Data
public class TicketEvent implements Event{
    private ClientInputData clientInputData;
    private String ticketID;
    private Stage stage;
    private List<String> errors;

    @SneakyThrows
    @Override
    public String toString() {
        return new ObjectMapper().writeValueAsString(this);
    }
}
