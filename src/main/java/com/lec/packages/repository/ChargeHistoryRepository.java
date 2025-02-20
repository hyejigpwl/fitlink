package com.lec.packages.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lec.packages.domain.ChargeHistory;

public interface ChargeHistoryRepository extends JpaRepository<ChargeHistory, String>{
	@Query("SELECT ch FROM ChargeHistory ch WHERE ch.memId = :memId order by ch.chargeDate desc")
	List<ChargeHistory> findByMemId(@Param("memId") String memId);
}
