package com.lec.packages.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDTO {
	
	 private LocalDateTime transferDate; // 송금일시
	 private BigDecimal amount;          // 금액
	 private String facilityName;        // 시설명
	 private String memId;               // 시설 소유자 ID
	 


}
