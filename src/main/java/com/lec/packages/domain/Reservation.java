package com.lec.packages.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Reservation extends BaseEntity{
    // Id => P.K 명시
    // @Column => 컬럼 정보 셋업
    // length => 컬럼 데이터 길이
    // name => 컬럼 이름
    // columnDefinition => 컬럼 데이터 타입 지정
    // @JoinColumn => 외래키 명시

	 	@Id
	    @Column(length = 20, name = "RESERVATION_CODE")
	    private String reservationCode;
	 	
	 	 @Column(name = "PAY_CODE")
		 private String payCode;
	 	
	 	@JoinColumn(name = "FACILITY_NAME")
	    @Column(nullable = false, name = "FACILITY_NAME", length = 50)
	    private String facilityName;

	 	@JoinColumn(name = "FACILITY_CODE")
	 	@Column(name = "FACILITY_CODE", length = 20)
	 	private String facilityCode;

	 	@JoinColumn(name = "MEM_ID")
	    @Column(name = "MEM_ID", length = 100)
	 	private String memId;

	    @Column(name = "RESERVATION_START_TIME", nullable = false)
	    private LocalTime reservationStartTime;
	    
	    @Column(name = "RESERVATION_END_TIME", nullable = false)
	    private LocalTime reservationEndTime;
	    
	    @Column(name = "RESERVATION_DATE" , nullable = false)
	    private Date reservationDate;
	    
	    @Column(name="COUNT", nullable=false)
	    private int count;

	    // DECIMAL TYPE 선언
	    // precision 소수점 앞자리
	    // scale 소수점 뒷자리
	    @JoinColumn(name = "PRICE")
	    @Column(name = "PRICE", columnDefinition = "DECIMAL", precision = 50, scale = 0)
	    private BigDecimal price;
	    
	    @Column(name = "memo", columnDefinition = "TEXT")
	    private String memo;
	    
	 	@Column(name = "RESERVATION_PROGRESS", length = 20)
	 	private String reservationProgress;

	    @Column(name = "DELETE_FLAG")
	    private boolean deleteFlag;
	    
	    @Column(length = 100, name = "CLUB_CODE")
	    private String clubCode;

//	    private LocalDateTime createDate;
	    
		public void setFormattedCreateDate(String format) {
			
		}

		

	    
	    
	    
}
