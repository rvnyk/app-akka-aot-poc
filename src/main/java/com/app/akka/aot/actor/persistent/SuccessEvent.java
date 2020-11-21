package com.app.akka.aot.actor.persistent;

import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import com.app.akka.aot.util.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class SuccessEvent implements Event {
    private ClientInputData clientInputData;
    private String ticketID;
}
