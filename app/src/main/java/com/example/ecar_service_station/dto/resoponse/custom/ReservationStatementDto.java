package com.example.ecar_service_station.dto.resoponse.custom;

import com.example.ecar_service_station.domain.Charger;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReservationStatementDto {

    private String reserveTitle;

    private String userName;

    private String carNumber;

    private Charger charger;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reservedAt;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chargeStartDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chargeEndDateTime;

    private String state;

    private Integer usedCashPoint;

    private Integer paidCash;

    private Integer cancellationFee;
}
