package com.app.akka.aot.actor;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.service.DownstreamService;
import com.app.akka.aot.util.Stage;
import com.app.akka.aot.util.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ClientAOTicket extends AbstractBehavior<Command> {

    private final String ticketID;
    private DownstreamService downstreamService;
    private Stage stage;
    private Status status;
    private ClientInputData clientInputData;
    private List<String> errors = new ArrayList<>();

    public static final EntityTypeKey<Command> TypeKey =
            EntityTypeKey.create(Command.class, "ClientAOTicket");

    public static void initSharding(ActorSystem<?> system) {
        ClusterSharding.get(system).init(Entity.of(TypeKey, entityContext ->
                ClientAOTicket.create(entityContext.getEntityId())
        ));
    }

    private ClientAOTicket(ActorContext<Command> context, String ticketID, DownstreamService downstreamService) {
        super(context);
        this.ticketID = ticketID;
        this.downstreamService = downstreamService;
    }

    public static Behavior<Command> create(String clientID){
        return Behaviors.setup(context-> new ClientAOTicket(context, clientID, new DownstreamService()));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AOTicketActorHandler.class, this::processAOticketStage)
                .onMessage(AOTicketSuccessHandler.class, this::processAOTicketSuccess)
                .onMessage(AOTTicketFailureHandler.class, this::processAOTicketFailures)
                .onMessage(QueryTicket.class, this::queryTicket)
                .build();
    }

    private Behavior<Command> queryTicket(QueryTicket queryTicket) {
        queryTicket.replyTo.tell(new AOTicketCurrentInfo(clientInputData,stage,status,errors));
        return this;
    }

    private Behavior<Command> processAOticketStage(AOTicketActorHandler aoTicketActorHandler){

        switch(aoTicketActorHandler.getStage()){
            case BASICINFO :
                this.stage = Stage.BASICINFO;
                this.status = Status.INPROGRESS;
                this.clientInputData = aoTicketActorHandler.getClientInputData();
                CompletionStage<Done> futureResultBasicProfile = downstreamService.createBasicProfile(aoTicketActorHandler.getClientInputData().getBasicInfo(), aoTicketActorHandler.getTicketID());
                handlefutureResponse(aoTicketActorHandler, futureResultBasicProfile,Stage.ADDRESS);
                break;
            case ADDRESS :
                this.stage = Stage.ADDRESS;
                this.status = Status.INPROGRESS;
                this.clientInputData = aoTicketActorHandler.getClientInputData();
                CompletionStage<Done> futureResultAddress = downstreamService.createAddress(aoTicketActorHandler.getClientInputData().getAddress(), aoTicketActorHandler.getTicketID());
                handlefutureResponse(aoTicketActorHandler, futureResultAddress, Stage.PHONE);
                break;
            case PHONE:
                this.stage = Stage.PHONE;
                this.status = Status.INPROGRESS;
                this.clientInputData = aoTicketActorHandler.getClientInputData();
                CompletionStage<Done> futureResultPhone = downstreamService.createPhone(aoTicketActorHandler.getClientInputData().getPhone(), aoTicketActorHandler.getTicketID());
                handlefutureResponse(aoTicketActorHandler, futureResultPhone, Stage.EMPLOYMENT);
                break;
            case EMPLOYMENT:
                this.stage = Stage.PHONE;
                this.status = Status.INPROGRESS;
                this.clientInputData = aoTicketActorHandler.getClientInputData();
                CompletionStage<Done> futureResultEmployment = downstreamService.createEmployment(aoTicketActorHandler.getClientInputData().getEmployment(), aoTicketActorHandler.getTicketID());
                handlefutureResponse(aoTicketActorHandler, futureResultEmployment, Stage.END);
                break;
        }
        return this;
    }

    private void handlefutureResponse(AOTicketActorHandler aoTicketActorHandler, CompletionStage<Done> futureResult, Stage stage ) {
        getContext()
                .pipeToSelf(
                        futureResult,
                        (ok, exc) -> {
                            if (exc == null){
                                this.status = Status.SUCCESS;
                                if(stage.equals(Stage.END)){
                                    aoTicketActorHandler.setStage(stage);
                                    this.stage = stage;
                                    return new AOTicketSuccessHandler(aoTicketActorHandler.getTicketID(), aoTicketActorHandler.getStage(), "Account Creation Completed Successfully", aoTicketActorHandler.getReplyTo());
                                }
                                else
                                    return new AOTicketActorHandler(aoTicketActorHandler.getClientInputData(),
                                            aoTicketActorHandler.getTicketID(),
                                            aoTicketActorHandler.getReplyTo(),
                                            stage);
                            }
                            else{
                                this.status = Status.FAILURE;
                                errors.add("Error interacting with " + aoTicketActorHandler.getStage().toString() + "  Handler Service");
                                return new AOTTicketFailureHandler(aoTicketActorHandler.getTicketID(),
                                        aoTicketActorHandler.getStage(),
                                        aoTicketActorHandler.getReplyTo(),
                                        "Error interacting with " + aoTicketActorHandler.getStage().toString() + "  Handler Service");
                            }
                        });
    }


    private Behavior<Command> processAOTicketFailures(AOTTicketFailureHandler ticketFailureHandler){
        ticketFailureHandler.replyTo.tell(new AOTResultData(ticketFailureHandler.getTicketID(),
                                                            ticketFailureHandler.getStage(),
                                                            ticketFailureHandler.getReason(),
                                                            Status.FAILURE
                                                            ));
        return this;
    }

    private Behavior<Command> processAOTicketSuccess(AOTicketSuccessHandler ticketSuccessHandler){
        ticketSuccessHandler.replyTo.tell(new AOTResultData(ticketSuccessHandler.getTicketID(),
                                                            ticketSuccessHandler.getStage(),
                                                            ticketSuccessHandler.getReason(),
                                                            Status.SUCCESS
        ));
        return this;
    }


}
