package com.example.ecar_service_station.dto.resoponse.custom.reservation;

import com.example.ecar_service_station.domain.Charger;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReserveResponseDto {

    private Long reservationId;

    private Long chargerId;

    private String userName;

    private String carNumber;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reservedAt;

    private String state;

    private Integer fares;
}
