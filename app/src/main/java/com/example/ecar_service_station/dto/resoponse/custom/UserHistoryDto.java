package com.example.ecar_service_station.dto.resoponse.custom;

import com.example.ecar_service_station.domain.Station;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserHistoryDto {

    private Station station;

    private Integer chargerCount;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime searchedAt;
}
