package com.lec.packages.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalTime;

import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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
@Table(name = "member_planner")
public class Member_Planner extends BaseEntity {
	@Id
	@Column(nullable = false, name = "PLAN_NO")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer planNo;
	
	@JoinColumn(name = "MEM_ID")
	@Column(name = "MEM_ID", length = 100)
	private String memId;

	@Column(nullable = false, name = "PLAN_DATE")
	private Date planDate;

	@Column(nullable = false, name = "PLAN_NAME", length = 50)
	private String planName;

	@Column(name = "PLAN_TEXT", columnDefinition = "TEXT")
	private String planText;

	// 운동한 날은 체크 표시
	@Column(name = "PLAN_ISCHK")
	private boolean planIschk;

	@Column(name = "PLAN_ISCLUB", length = 20)
	private boolean planIsclub;

	@JoinColumn(name = "RESERVATION_CODE")
	@Column(length = 20, name = "RESERVATION_CODE")
	private String reservationCode;

	@Column(name = "DELETE_FLAG")
	private Boolean deleteFlag = false;
	



}
