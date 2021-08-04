package com.example.ecar_service_station.dto.resoponse.custom;

import lombok.Data;

@Data
public class RegisteredBankAccountDto {

    private Long bankId;

    private String bankName;

    private String bankAccountNumber;

    private String msg;
}
