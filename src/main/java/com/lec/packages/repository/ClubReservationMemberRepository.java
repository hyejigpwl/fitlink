package com.lec.packages.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.lec.packages.domain.Reservation;
import com.lec.packages.domain.Reservation_Member_List;
import com.lec.packages.domain.primaryKeyClasses.ClubReservationMemberKeyClass;

public interface ClubReservationMemberRepository
		extends JpaRepository<Reservation_Member_List, ClubReservationMemberKeyClass> {

	@Query("SELECT m FROM Reservation_Member_List m WHERE m.memId = :memId and m.deleteFlag is null and m.clubCode = :clubCode")
	List<Reservation_Member_List> findByMemId(@Param("memId") String memId,@Param("clubCode") String clubCode);

	@Query("SELECT r FROM Reservation_Member_List r "
			+ "JOIN FETCH Reservation res ON r.reservationCode = res.reservationCode " + "WHERE r.memId = :memId and r.deleteFlag is null")
	List<Reservation_Member_List> findClubReservationsWithDetails(@Param("memId") String memId);

	@Query("SELECT COUNT(m) > 0 FROM Member_Planner m WHERE m.reservationCode = :reservationCode and m.memId = :memId and m.deleteFlag is null")
	boolean existsByReservationCode(@Param("reservationCode") String reservationCode, @Param("memId") String memId);
	
	@Query("SELECT COUNT(r) > 0 FROM Reservation_Member_List r WHERE r.reservationCode = :reservationCode and r.memId = :memId and r.deleteFlag is null")
	boolean findExistRes(@Param("reservationCode") String reservationCode, @Param("memId") String memId);

	@Query("SELECT r FROM Reservation_Member_List r where r.reservationCode= :reservationCode and r.memId = :memId and r.clubCode = :clubCode and r.deleteFlag is null")
	Optional<Reservation_Member_List> findResClubMemListWithDetails(@Param("reservationCode") String reservationCode, @Param("clubCode") String clubCode,
			@Param("memId") String memId);
	
	Optional<Reservation_Member_List> findByClubCodeAndMemId(String clubCode,String memId);
	
	@Modifying
	@Transactional
	@Query("UPDATE Reservation_Member_List r SET r.deleteFlag = true WHERE r.reservationCode = :reservationCode AND r.clubCode = :clubCode AND r.memId = :memId and r.deleteFlag IS NULL")
	int updateReservationMemberFlag(@Param("reservationCode") String reservationCode, 
	                                @Param("clubCode") String clubCode, 
	                                @Param("memId") String memId);

	
	@Query("SELECT r FROM Reservation r where r.memId = :memId and r.clubCode is null and r.deleteFlag = false")
	List<Reservation> findMemberReservationsWithDetails(@Param("memId") String memId);


}