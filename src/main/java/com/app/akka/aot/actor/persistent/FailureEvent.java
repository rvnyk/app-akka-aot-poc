package com.app.akka.aot.actor.persistent;

import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class FailureEvent implements Event{
    private String ticketID;
    private ClientInputData clientInputData;
    private Stage stage;
    private String error;
}
