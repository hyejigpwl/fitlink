package com.lec.packages.dto;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPlannerDTO {
	private Integer planNo;

	private String memId;

	private Date planDate;

	private String planName;

	private String planText;

	private boolean planIschk;

	private boolean planIsclub;

	private String reservationCode;

	private Boolean deleteFlag;
}
