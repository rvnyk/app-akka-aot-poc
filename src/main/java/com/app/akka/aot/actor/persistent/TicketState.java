package com.app.akka.aot.actor.persistent;

import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import com.app.akka.aot.util.Status;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TicketState {
    private Stage stage;
    private Status status;
    private ClientInputData clientInputData;
    private String error;
}
