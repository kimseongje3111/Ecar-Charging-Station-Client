package com.example.ecar_service_station.dto.request.bank;

import lombok.Data;

@Data
public class RegisterBankAccountDto {

    private String bankName;

    private String bankAccountNumber;

    private String bankAccountOwner;

    private Long certificateId;

    private String certificatePassword;
}
