package com.example.ecar_service_station.dto.request.reservation;

import lombok.Data;

@Data
public class PayReservationDto {

    private Long reservationId;

    private Integer usedCashPoint;
}
