package com.app.akka.aot.model;

import lombok.Data;

@Data
public class ClientInputData {
    private BasicInfo basicInfo;
    private Address address;
    private Employment employment;
    private Phone phone;
}
