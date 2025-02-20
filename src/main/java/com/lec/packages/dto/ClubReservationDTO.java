package com.lec.packages.dto;

import java.time.LocalTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubReservationDTO {
    String reservationCode;
    String facilityName;
    String clubCode;
    Integer count;
    Integer nowMemCount;
    String memberList; 
    LocalTime reservationStartTime;
    Date reservationDate;
    String ReservationProgress;
}
