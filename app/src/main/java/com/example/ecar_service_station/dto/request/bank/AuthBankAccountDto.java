package com.example.ecar_service_station.dto.request.bank;

import lombok.Data;

@Data
public class AuthBankAccountDto {

    private Long bankId;

    private String paymentPassword;

    private String authMsg;
}
