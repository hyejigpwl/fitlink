package com.lec.packages.repository;

import com.lec.packages.domain.Member_Planner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberPlannerRepository extends JpaRepository<Member_Planner, Integer> {

    // 특정 회원이 등록한 일정 조회 (클럽일정x, 시설예약일정x)
	@Query("SELECT m FROM Member_Planner m WHERE m.memId = :memId and m.deleteFlag = false and m.planIsclub = false and m.reservationCode is null")
    List<Member_Planner> findByMemId(@Param("memId") String memId);

    // 특정 회원의 특정 날짜 일정 조회
    @Query("SELECT m FROM Member_Planner m WHERE m.memId = :memId and m.planDate = :planDate and m.deleteFlag = false")
    List<Member_Planner> findByMemIdAndPlanDate(@Param("memId") String memId,@Param("planDate") Date planDate);
    
    @Query("SELECT m FROM Member_Planner m WHERE m.planNo = :planNo and m.deleteFlag = false")
    Optional<Member_Planner> findByPlanNoAndDeleteFlagFalse(@Param("planNo") Integer planNo);

    @Query("SELECT m FROM Member_Planner m WHERE m.reservationCode = :reservationCode AND m.memId = :memId")
    Optional<Member_Planner> findFirstByReservationCodeAndMemId(@Param("reservationCode") String reservationCode, @Param("memId") String memId);

    
    @Query("SELECT m FROM Member_Planner m WHERE m.reservationCode = :reservationCode AND m.memId = :memId and m.deleteFlag = true")
    Optional<Member_Planner> findByReservationCodeAndMemIAndDeleteFlagTrue(@Param("reservationCode") String reservationCode, @Param("memId") String memId);
    
    @Query("SELECT m FROM Member_Planner m WHERE m.reservationCode = :reservationCode AND m.memId = :memId and m.deleteFlag = false")
    Optional<Member_Planner> findByReservationCodeAndMemIAndDeleteFlagFalse(@Param("reservationCode") String reservationCode, @Param("memId") String memId);


	List<Member_Planner> findByReservationCodeAndMemIdAndDeleteFlagTrue(String reservationCode, String memId);
	List<Member_Planner> findByReservationCodeIsNullAndMemIdAndDeleteFlagFalse(String reservationCode);

	List<Member_Planner> findByReservationCodeAndMemIdAndDeleteFlagFalse(String reservationCode, String memId);	
	List<Member_Planner> findByReservationCodeIsNullAndMemIdAndDeleteFlagTrue(String memId);


}
