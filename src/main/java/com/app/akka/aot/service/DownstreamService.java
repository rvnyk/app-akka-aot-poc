package com.app.akka.aot.service;

import akka.Done;
import com.app.akka.aot.model.Address;
import com.app.akka.aot.model.BasicInfo;
import com.app.akka.aot.model.Employment;
import com.app.akka.aot.model.Phone;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class DownstreamService {


    public CompletionStage<Done> createBasicProfile(BasicInfo basicInfo, String clientID){
        final CompletableFuture<Done> callBasicService = new CompletableFuture<>();
        //Runtime.getRuntime().addShutdownHook(new Thread(() -> callBasicService.complete(Done.getInstance())));
        CompletableFuture.runAsync(() -> {
            System.out.println("Calling Basic Profile Service for clientID : " + clientID);
            if(basicInfo.getFirstName().equalsIgnoreCase("Fail")){
                callBasicService.completeExceptionally(new Exception("Stubbed to fail basic Info Call"));
            }
            else
                callBasicService.complete(Done.getInstance());
        });
        return callBasicService;
    }

    public CompletionStage<Done> createAddress(Address address, String clientID){
        final CompletableFuture<Done> callAddressService = new CompletableFuture<>();
        //Runtime.getRuntime().addShutdownHook(new Thread(() -> callBasicService.complete(Done.getInstance())));
        CompletableFuture.runAsync(() -> {
            System.out.println("Calling Address Service for clientID : " + clientID);
            if(address.getAddressLine1().equalsIgnoreCase("Fail")){
                callAddressService.completeExceptionally(new Exception("Stubbed to fail Address Service  Call"));
            }
            else
                callAddressService.complete(Done.getInstance());
        });
        return callAddressService;
    }

    public CompletionStage<Done> createPhone(Phone phone, String clientID){
        final CompletableFuture<Done> callPhoneService = new CompletableFuture<>();
        //Runtime.getRuntime().addShutdownHook(new Thread(() -> callBasicService.complete(Done.getInstance())));
        CompletableFuture.runAsync(() -> {
            System.out.println("Calling Phone Service for clientID : " + clientID);
            callPhoneService.complete(Done.getInstance());
        });
        return callPhoneService;
    }

    public CompletionStage<Done> createEmployment(Employment employment, String clientID){
        final CompletableFuture<Done> callEmploymentService = new CompletableFuture<>();
        //Runtime.getRuntime().addShutdownHook(new Thread(() -> callBasicService.complete(Done.getInstance())));
        CompletableFuture.runAsync(() -> {
            System.out.println("Calling Employment Service for clientID : " + clientID);
            callEmploymentService.complete(Done.getInstance());
        });
        return callEmploymentService;
    }
}
