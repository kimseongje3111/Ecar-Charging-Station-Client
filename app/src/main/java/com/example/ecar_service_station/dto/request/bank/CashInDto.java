package com.example.ecar_service_station.dto.request.bank;

import lombok.Data;

@Data
public class CashInDto {

    private Integer amount;

    private String paymentPassword;
}
