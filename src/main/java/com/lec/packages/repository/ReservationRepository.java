package com.lec.packages.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.lec.packages.domain.Member;
import com.lec.packages.domain.Reservation;
import com.lec.packages.dto.ClubReservationInterface;

public interface ReservationRepository extends JpaRepository<Reservation, String> {

	Optional<Reservation> findByReservationCode(@Param("reservationCode") String reservationCode);
	

	Page<Reservation> searchByMemId(String memId, Pageable pageable);

	@Query("SELECT r FROM Reservation r " + "WHERE r.memId = :memId " + "ORDER BY " + "CASE r.reservationProgress "
			+ "    WHEN '예약진행중' THEN 1 " + "    WHEN '예약완료' THEN 2 " + "    WHEN '예약취소' THEN 3 " + "    ELSE 4 " + // 예상치
																													// 못한
																													// 상태를
																													// 처리
			"END, " + "r.CREATEDATE DESC")
	List<Reservation> findByMemId(@Param("memId") String memId);

	List<Reservation> findByFacilityCode(String facilityCode);

	//유저의 시설 중 모든 예약 상태 예약 목록 노출
	@Query("SELECT r FROM Reservation r "
		       + "JOIN Facility f ON r.facilityCode = f.facilityCode "
		       + "WHERE f.memId = :memId "
		       + "ORDER BY r.CREATEDATE DESC")
	Page<Reservation> findAllReservationsWithUser(@Param("memId") String memId, Pageable pageable);
	
	//유저의 시설 중 모든 예약 상태 예약 목록 리스트로 뽑기
	@Query("SELECT r FROM Reservation r "
			+ "JOIN Facility f ON r.facilityCode = f.facilityCode "
			+ "WHERE f.memId = :memId ")
	List<Reservation> findAllReservationsWithUserToList(@Param("memId") String memId);
	
	//유저의 시설 중 예약완료 상태 예약 목록 노출
	@Query("SELECT r FROM Reservation r "
		       + "JOIN Facility f ON r.facilityCode = f.facilityCode "
		       + "WHERE f.memId = :memId "
		       + "AND r.reservationProgress = '예약완료' "
		       + "ORDER BY r.CREATEDATE DESC")
	Page<Reservation> findConfirmReservationsWithUser(@Param("memId") String memId, Pageable pageable);
	
	//유저의 시설 중 예약취소 상태 예약 목록 노출
	@Query("SELECT r FROM Reservation r "
			+ "JOIN Facility f ON r.facilityCode = f.facilityCode "
			+ "WHERE f.memId = :memId "
			+ "AND r.reservationProgress = '예약취소' "
			+ "ORDER BY r.CREATEDATE DESC")
	Page<Reservation> findRefuseReservationsWithUser(@Param("memId") String memId, Pageable pageable);
	
	//유저의 시설 중 예약진행중 상태 예약 목록 노출
	@Query("SELECT r FROM Reservation r "
			+ "JOIN Facility f ON r.facilityCode = f.facilityCode "
			+ "WHERE f.memId = :memId "
			+ "AND r.reservationProgress = '예약진행중' "
			+ "ORDER BY r.CREATEDATE DESC")
	Page<Reservation> findInprogressReservationsWithUser(@Param("memId") String memId, Pageable pageable);


	@Query(value = "SELECT \r\n"
	        + "    r.*, \r\n"
	        + "    IFNULL(COUNT(DISTINCT CASE WHEN rml.DELETE_FLAG is null THEN rml.MEM_ID END), 0) AS nowMemCount, \r\n"
	        + "    JSON_ARRAYAGG(\r\n"
	        + "        JSON_OBJECT(\r\n"
	        + "            'memId', m.MEM_ID,\r\n"
	        + "            'memNickname', m.MEM_NICKNAME,\r\n"
	        + "            'memPicture', m.MEM_PICTURE,\r\n"
	        + "            'memSocial', m.MEM_SOCIAL\r\n"
	        + "        )\r\n"
	        + "    ) AS memberList\r\n"
	        + "FROM reservation r\r\n"
	        + "LEFT JOIN reservation_member_list rml \r\n"
	        + "    ON r.club_code = rml.CLUB_CODE \r\n"
	        + "    AND r.RESERVATION_CODE = rml.RESERVATION_CODE\r\n"
	        + "    AND rml.DELETE_FLAG IS NULL\r\n"
	        + "LEFT JOIN member m \r\n"
	        + "    ON rml.MEM_ID = m.MEM_ID \r\n"
	        + "WHERE \r\n"
	        + "    r.club_code = :clubCode \r\n"
	        + "    AND r.RESERVATION_DATE > NOW() \r\n"
	        + "    AND r.RESERVATION_PROGRESS = '예약완료'\r\n"
	        + "GROUP BY r.RESERVATION_CODE;\r\n",
	        nativeQuery = true)
	List<ClubReservationInterface> getClubResList(@Param("clubCode") String clubCode);


	List<Reservation> findByFacilityCodeAndReservationDateAndDeleteFlagOrderByReservationStartTime(String facilityCode,
			Date reservationDate, boolean deleteFlag);

	// 예약 테이블의 memId를 통해 Member 테이블의 정보를 조회
	@Query("SELECT m FROM Member m WHERE m.memId = :memId")
	Member findMemberByReservationMemId(@Param("memId") String memId);

	// 로그인 한 유저의 예약 승인 된 시설 만 노출
	@Query("SELECT r FROM Reservation r "
			+ "WHERE r.facilityCode IN (SELECT f.facilityCode FROM Facility f WHERE f.memId = :memId) "
			+ "AND r.reservationProgress = '예약완료'")
	List<Reservation> findConfirmedReservationsByMemId(@Param("memId") String memId);

	// 예약코드로 시설이름, 시설운영시간 조회
	@Query("SELECT r.facilityName, r.reservationStartTime, r.reservationEndTime "
			+ "FROM Reservation r WHERE r.reservationCode = :reservationCode")
	List<Object[]> findFacilityAndTimesByCode(@Param("reservationCode") String reservationCode);

	// List<Reservation> findAllByDeleteFlag(boolean deleteFlag);
	
	 // Bulk Update를 활용한 빠른 삭제
    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.deleteFlag = true WHERE r.reservationDate < :today AND r.deleteFlag = false")
    int markOldReservationsAsDeleted(@Param("today") LocalDate today);

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate < :date AND r.reservationProgress = :status")
    List<Reservation> findReservationDateBeforeAndStatus( @Param("date") LocalDate date, @Param("status") String status);

    
    @Query("SELECT r FROM Reservation r "
			+ "WHERE r.payCode = :payCode and r.deleteFlag is true")
	Optional<Reservation> findByPayCodeAndDeleteFlag(@Param("payCode") String payCode);


	boolean existsByReservationCodeAndReservationProgress(String reservationCode, String string);
}
