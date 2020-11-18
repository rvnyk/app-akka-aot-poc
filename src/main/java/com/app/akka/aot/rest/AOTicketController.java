package com.app.akka.aot.rest;

import com.app.akka.aot.actor.AOTicketResult;
import com.app.akka.aot.model.ClientInputData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AOTicketController {

    @PostMapping("/aot/submit")
    public ResponseEntity<AOTicketResult>  submitAOTicket(@RequestBody ClientInputData clientInputData){
        return null;
    }

    @GetMapping("/aot/query/{id}")
    public ResponseEntity<AOTicketResult> queryTicket(@PathVariable Long ticketid){
        return null;
    }
}
