package com.app.akka.aot.rest;

import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.app.akka.aot.actor.*;
import com.app.akka.aot.actor.cluster.Guardian;
import com.app.akka.aot.actor.memory.ClientAOTicket;
import com.app.akka.aot.actor.persistent.ClientAOTicketPersistent;
import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.util.Stage;
import com.app.akka.aot.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class AOTicketController {

    @Autowired
    private Guardian guardian;

    @Async
    @PostMapping("/aot/submit")
    public CompletableFuture<ResponseEntity<AOTResultData>> submitAOTicket(@RequestBody ClientInputData clientInputData){
        long min = 1000000000L;
        long max = 9999999999L;
        Long ticketIDL = new Random().nextLong() % (max - min) + max;
        String ticketID = Long.toString(ticketIDL);
        System.out.println("Submitting client Data with ticketID : " + ticketID);
        //EntityRef<Command> ref = guardian.getSharding().entityRefFor(ClientAOTicket.TypeKey, ticketID);
        EntityRef<Command> ref = guardian.getSharding().entityRefFor(ClientAOTicketPersistent.TypeKey, ticketID);
        //return ref.<AOTResultData>ask(replyTo-> new AOTicketActorHandler(clientInputData, ticketID, replyTo, Stage.BASICINFO), guardian.getTimeout());

        return CompletableFuture.supplyAsync(() -> {
            try {
                return ResponseEntity.accepted().body(ref.<AOTResultData>ask(replyTo-> new AOTicketActorHandler(clientInputData, ticketID, replyTo, Stage.BASICINFO), guardian.getTimeout()).toCompletableFuture().get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return ResponseEntity.accepted().body(new AOTResultData(ticketID, Stage.BASICINFO, "In Progress", Status.INPROGRESS));
        });
    }

    @Async
    @PostMapping("/aot/submit/{ticketid}")
    public CompletableFuture<ResponseEntity<AOTResultData>> updateAOTicket(@PathVariable(name = "ticketid") String ticketid, @RequestParam(name = "stage") Stage stage, @RequestBody ClientInputData clientInputData){
        System.out.println("Updating data client Data for ticketID : " + ticketid);
        //EntityRef<Command> ref = guardian.getSharding().entityRefFor(ClientAOTicket.TypeKey, ticketID);
        EntityRef<Command> ref = guardian.getSharding().entityRefFor(ClientAOTicketPersistent.TypeKey, ticketid);
        //return ref.<AOTResultData>ask(replyTo-> new AOTicketActorHandler(clientInputData, ticketID, replyTo, Stage.BASICINFO), guardian.getTimeout());
        return CompletableFuture.supplyAsync(() -> {
            try {
                return ResponseEntity.accepted().body(ref.<AOTResultData>ask(replyTo-> new AOTicketActorHandler(clientInputData, ticketid, replyTo, stage), guardian.getTimeout()).toCompletableFuture().get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return ResponseEntity.accepted().body(new AOTResultData(ticketid, Stage.BASICINFO, "In Progress", Status.INPROGRESS));
        });
    }

    @Async
    @GetMapping("/aot/query/{ticketid}")
    public CompletableFuture<ResponseEntity<AOTicketCurrentInfo>> queryTicket(@PathVariable Long ticketid){
        //EntityRef<Command> ref = guardian.getSharding().entityRefFor(ClientAOTicket.TypeKey, ticketid.toString());
        EntityRef<Command> ref = guardian.getSharding().entityRefFor(ClientAOTicketPersistent.TypeKey, ticketid.toString());

        return CompletableFuture.supplyAsync(() -> {
            try {
                return ResponseEntity.accepted().body(ref.<AOTicketCurrentInfo>ask(replyTo-> new QueryTicket(ticketid.toString(), replyTo), guardian.getTimeout()).toCompletableFuture().get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return ResponseEntity.accepted().body(new AOTicketCurrentInfo(null, Stage.BASICINFO, Status.FAILURE, Arrays.asList("Something went wrong!")));
        });

        //return new ResponseEntity(new AOTResultData(ticketid.toString(), Stage.BASICINFO, "In Progress", Status.INPROGRESS) , HttpStatus.OK);
    }
}
