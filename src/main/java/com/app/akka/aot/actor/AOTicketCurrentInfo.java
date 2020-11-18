package com.app.akka.aot.actor;

import com.app.akka.aot.actor.AOTicketResult;
import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import com.app.akka.aot.util.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AOTicketCurrentInfo implements AOTicketResult {
    private ClientInputData clientInputData;
    private Stage stage;
    private Status status;
    private List<String> errors;
}
