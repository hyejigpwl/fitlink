package com.lec.packages.domain;

import java.sql.Date;
import java.time.LocalTime;

import com.lec.packages.domain.primaryKeyClasses.ClubReservationMemberKeyClass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ClubReservationMemberKeyClass.class)
public class Reservation_Member_List extends BaseEntity{
    @Id
    @JoinColumn(name = "RESERVATION_CODE")
    @Column(nullable = false, length = 20, name = "RESERVATION_CODE")
    private String reservationCode;

    @Id
    @JoinColumn(name = "CLUB_CODE")
    @Column(nullable = false, length = 10, name = "CLUB_CODE")
    private String clubCode;

    @Id
    @JoinColumn(name = "MEM_ID")
    @Column(nullable = false, length = 100, name = "MEM_ID")
    private String memId;

    @Column(name = "RESERVATION_TIME", nullable = false)
    private LocalTime reservationTime;

    @Column(name = "DELETE_FLAG")
    private Boolean deleteFlag;
    
    @Column(name = "RESERVATION_DATE" , nullable = false)
    private Date reservationDate;
    
    public void removeClubResMember(boolean deleteFlag) {
		this.deleteFlag = true;		
	}
}
