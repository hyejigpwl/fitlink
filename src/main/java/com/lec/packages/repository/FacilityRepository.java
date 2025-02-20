package com.lec.packages.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lec.packages.domain.Facility;


public interface FacilityRepository extends JpaRepository<Facility,String>{
	
    boolean existsByFacilityCode(String facilityCode);
  
    Optional<Facility> findByFacilityCode(@Param("facilityCode") String facilityCode);

    //유저별 시설 목록 보기
    @Query("SELECT f FROM Facility f WHERE f.memId= :memId ORDER BY f.facilityCode ASC")
    Page<Facility> searchByUser(@Param("memId") String memId,Pageable pageable);
    
    List<Facility> findByMemId(String memId);
    
    // 시설검색
    @Query("SELECT f FROM Facility f WHERE f.deleteFlag = false "
    		+ "AND (:address IS NULL OR f.facilityAddress LIKE %:address%) "
    		+ "AND (:exerciseCode IS NULL OR f.exerciseCode LIKE %:exerciseCode%) "
    		+ "AND (:isOnlyClub IS NULL OR f.facilityIsOnlyClub = :isOnlyClub) ")
    Page<Facility> searchAll(@Param("address") String facilityAddress, @Param("exerciseCode") String exerciseCode,
    					 @Param("isOnlyClub") Boolean facilityIsOnlyClub, Pageable pageable);
    
    // 사설시설검색
    @Query("SELECT f FROM Facility f WHERE f.deleteFlag = false "
    		+ "AND (:address IS NULL OR f.facilityAddress LIKE %:address%) "
    		+ "AND (:exerciseCode IS NULL OR f.exerciseCode LIKE %:exerciseCode%) "
    		+ "AND (:isOnlyClub IS NULL OR f.facilityIsOnlyClub = :isOnlyClub) "
    		+ "AND f.facilityCode LIKE 'FACIL%'")
    Page<Facility> searchPrivate(@Param("address") String facilityAddress, @Param("exerciseCode") String exerciseCode,
    					 @Param("isOnlyClub") Boolean facilityIsOnlyClub, Pageable pageable);   
    
    // 공공시설검색
    @Query("SELECT f FROM Facility f WHERE f.deleteFlag = false "
    		+ "AND (:address IS NULL OR f.facilityAddress LIKE %:address%) "
    		+ "AND (:exerciseCode IS NULL OR f.exerciseCode LIKE %:exerciseCode%) "
    		+ "AND (:isOnlyClub IS NULL OR f.facilityIsOnlyClub = :isOnlyClub) "
    		+ "AND f.facilityCode LIKE '00DATA%'")
    Page<Facility> searchPublic(@Param("address") String facilityAddress, @Param("exerciseCode") String exerciseCode,
    					 @Param("isOnlyClub") Boolean facilityIsOnlyClub, Pageable pageable);
  
    // 공공데이터 위치기반 시설
    @Query("SELECT f FROM Facility f WHERE f.facilityCode LIKE '00DATA%' AND f.facilityLat IS NOT NULL AND f.facilityLongt IS NOT NULL AND f.deleteFlag = false "
    		+ "AND FUNCTION('ST_Distance_Sphere', POINT(f.facilityLongt, f.facilityLat), POINT(:longt, :lat)) <= :radius * 1000")
    List<Facility> findFacilityWithRadius(@Param("lat") BigDecimal lat, @Param("longt") BigDecimal longt, @Param("radius") double radius);
    
    // 사설기반 시설
    @Query("SELECT f FROM Facility f WHERE f.facilityCode LIKE 'FACIL%' AND f.facilityLat IS NOT NULL AND f.facilityLongt IS NOT NULL AND f.deleteFlag = false "
    		+ "AND FUNCTION('ST_Distance_Sphere', POINT(f.facilityLongt, f.facilityLat), POINT(:longt, :lat)) <= :radius * 1000")
    List<Facility> findPrivateFacilityWithRadius(@Param("lat") BigDecimal lat, @Param("longt") BigDecimal longt, @Param("radius") double radius);

    @Query("SELECT f FROM Facility f WHERE f.facilityCode LIKE 'FACIL%'")
    List<Facility> findAll();
    
    @Query("SELECT f FROM Facility f WHERE f.facilityCode LIKE '00DATA%'")
    List<Facility> findPublic();


}
