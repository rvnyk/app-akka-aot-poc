package com.app.akka.aot.rest;

import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.app.akka.aot.actor.*;
import com.app.akka.aot.actor.cluster.Guardian;
import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RestController
public class AOTicketController {

    @Autowired
    private Guardian guardian;

    @Async
    @PostMapping("/aot/submit")
    public CompletableFuture<ResponseEntity<CompletionStage<AOTResultData>>> submitAOTicket(@RequestBody ClientInputData clientInputData){
        long min = 1000000000L;
        long max = 9999999999L;
        Long ticketIDL = new Random().nextLong() % (max - min) + max;
        String ticketID = Long.toString(ticketIDL);
        System.out.println("Submitting client Data with ticketID : " + ticketID);
        EntityRef<Command> ref = guardian.getSharding().entityRefFor(ClientAOTicket.TypeKey, ticketID);
        //return ref.<AOTResultData>ask(replyTo-> new AOTicketActorHandler(clientInputData, ticketID, replyTo, Stage.BASICINFO), guardian.getTimeout());

        return CompletableFuture.supplyAsync(() -> ResponseEntity.accepted().body(ref.<AOTResultData>ask(replyTo-> new AOTicketActorHandler(clientInputData, ticketID, replyTo, Stage.BASICINFO), guardian.getTimeout())));
    }

    @GetMapping("/aot/query/{ticketid}")
    public CompletableFuture<ResponseEntity<CompletionStage<AOTicketCurrentInfo>>> queryTicket(@PathVariable Long ticketid){
        EntityRef<Command> ref = guardian.getSharding().entityRefFor(ClientAOTicket.TypeKey, ticketid.toString());

        return CompletableFuture.supplyAsync(() -> ResponseEntity.accepted().body(ref.<AOTicketCurrentInfo>ask(replyTo-> new QueryTicket(ticketid.toString(), replyTo), guardian.getTimeout())));

        //return new ResponseEntity(new AOTResultData(ticketid.toString(), Stage.BASICINFO, "In Progress", Status.INPROGRESS) , HttpStatus.OK);
    }
}
