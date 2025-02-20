package com.lec.packages.dto;

import java.time.LocalTime;
import java.util.Date;

public interface ClubReservationInterface {
    String getReservationCode();
    String getFacilityName();
    String getClubCode();
    Integer getCount();
    Integer getNowMemCount();
    String getMemberList(); 
    LocalTime getReservationStartTime();
    Date getReservationDate();
    String getReservationProgress();
}
