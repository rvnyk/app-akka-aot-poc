package com.app.akka.aot.actor.persistent;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Behavior;
import akka.persistence.typed.PersistenceId;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.app.akka.aot.actor.*;
import com.app.akka.aot.actor.memory.ClientAOTicket;
import com.app.akka.aot.model.ClientInputData;
import com.app.akka.aot.service.DownstreamService;
import com.app.akka.aot.util.Stage;
import com.app.akka.aot.util.Status;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ClientAOTicketPersistent
        extends EventSourcedBehavior<Command, Event, TicketState> {

    private final String ticketID;
    private ActorContext<Command> context;
    private DownstreamService downstreamService;

    public static final EntityTypeKey<Command> TypeKey =
            EntityTypeKey.create(Command.class, "ClientAOTicket");


    public static void initSharding(ActorSystem<?> system) {
        ClusterSharding.get(system).init(Entity.of(TypeKey, entityContext ->
                ClientAOTicketPersistent.create(entityContext.getEntityId())
        ));
    }

    private ClientAOTicketPersistent(String ticketID, ActorContext<Command> context, DownstreamService downstreamService) {
        super(PersistenceId.of("ClientAOTicket", ticketID),
                SupervisorStrategy.restartWithBackoff(Duration.ofMillis(200), Duration.ofSeconds(5), 0.1));
        this.ticketID = ticketID;
        this.context = context;
        this.downstreamService = downstreamService;
    }

    public static Behavior<Command> create(String clientID){
        return Behaviors.setup(context-> new ClientAOTicketPersistent(clientID, context, new DownstreamService()));
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

    @Override
    public CommandHandler<Command, Event, TicketState> commandHandler() {
        return newCommandHandlerBuilder().forAnyState()
                .onCommand(AOTicketActorHandler.class, (state, cmd) -> Effect().persist(new TicketEvent(cmd.getClientInputData(),cmd.getTicketID(),cmd.getStage(),null)).thenRun(state2 -> {
                    processAOticketStage(cmd);
                })).onCommand(AOTicketSuccessHandler.class, (state, cmd) -> Effect().persist(new SuccessEvent(cmd.getClientInputData(),cmd.getTicketID())).thenRun(state2 -> {
                    processAOTicketSuccess(cmd);
                })).onCommand(AOTTicketFailureHandler.class, (state, cmd) -> Effect().persist(new FailureEvent(cmd.getTicketID(),cmd.getClientInputData(), cmd.getStage(),cmd.getReason())).thenRun(state2 -> {
                    processAOTicketFailures(cmd);
                })).onCommand(QueryTicket.class, (state, cmd) -> {
                    queryTicket(state, cmd);
                    return Effect().none();
                }).build();
    }

    @Override
    public EventHandler<TicketState, Event> eventHandler() {
        return newEventHandlerBuilder().forAnyState()
                .onEvent(TicketEvent.class, (state, event) ->
                            TicketState.builder().stage(event.getStage()).status(Status.INPROGRESS).clientInputData(event.getClientInputData()).build()

                ).onEvent(SuccessEvent.class, (state, event) ->
                        TicketState.builder().clientInputData(event.getClientInputData()).stage(Stage.END).status(Status.SUCCESS).build()

                ).onEvent(FailureEvent.class, (state, event) ->
                TicketState.builder().clientInputData(event.getClientInputData()).stage(event.getStage()).status(Status.FAILURE).error(event.getError()).build()

        ).build();
    }

    @Override
    public TicketState emptyState() {
        return TicketState.builder().build();
    }


    private Behavior<Command> queryTicket(TicketState state, QueryTicket queryTicket) {
        queryTicket.replyTo.tell(new AOTicketCurrentInfo(state.getClientInputData(),state.getStage(),state.getStatus(), Arrays.asList(state.getError())));
        return this;
    }

    private Behavior<Command> processAOticketStage(AOTicketActorHandler aoTicketActorHandler){

        switch(aoTicketActorHandler.getStage()){
            case BASICINFO :
                CompletionStage<Done> futureResultBasicProfile = downstreamService.createBasicProfile(aoTicketActorHandler.getClientInputData().getBasicInfo(), aoTicketActorHandler.getTicketID());
                handlefutureResponse(aoTicketActorHandler, futureResultBasicProfile,Stage.ADDRESS);
                break;
            case ADDRESS :
                CompletionStage<Done> futureResultAddress = downstreamService.createAddress(aoTicketActorHandler.getClientInputData().getAddress(), aoTicketActorHandler.getTicketID());
                handlefutureResponse(aoTicketActorHandler, futureResultAddress, Stage.PHONE);
                break;
            case PHONE:
                CompletionStage<Done> futureResultPhone = downstreamService.createPhone(aoTicketActorHandler.getClientInputData().getPhone(), aoTicketActorHandler.getTicketID());
                handlefutureResponse(aoTicketActorHandler, futureResultPhone, Stage.EMPLOYMENT);
                break;
            case EMPLOYMENT:
                CompletionStage<Done> futureResultEmployment = downstreamService.createEmployment(aoTicketActorHandler.getClientInputData().getEmployment(), aoTicketActorHandler.getTicketID());
                handlefutureResponse(aoTicketActorHandler, futureResultEmployment, Stage.END);
                break;
        }
        return this;
    }

    private void handlefutureResponse(AOTicketActorHandler aoTicketActorHandler, CompletionStage<Done> futureResult, Stage stage ) {
        context
                .pipeToSelf(
                        futureResult,
                        (ok, exc) -> {
                            if (exc == null){
                                if(stage.equals(Stage.END)){
                                    aoTicketActorHandler.setStage(stage);
                                    return new AOTicketSuccessHandler(aoTicketActorHandler.getTicketID(), aoTicketActorHandler.getStage(), "Account Creation Completed Successfully", aoTicketActorHandler.getClientInputData(),  aoTicketActorHandler.getReplyTo());
                                }
                                else
                                    return new AOTicketActorHandler(aoTicketActorHandler.getClientInputData(),
                                            aoTicketActorHandler.getTicketID(),
                                            aoTicketActorHandler.getReplyTo(),
                                            stage);
                            }
                            else{
                                return new AOTTicketFailureHandler(aoTicketActorHandler.getTicketID(),
                                        aoTicketActorHandler.getStage(),
                                        "Error interacting with " + aoTicketActorHandler.getStage().toString() + "  Handler Service",
                                        aoTicketActorHandler.getClientInputData(),
                                        aoTicketActorHandler.getReplyTo());
                            }
                        });
    }





}
