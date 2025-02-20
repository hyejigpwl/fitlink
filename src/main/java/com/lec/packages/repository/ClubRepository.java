package com.lec.packages.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lec.packages.domain.Club;

public interface ClubRepository extends JpaRepository<Club, String> {
	
    // deleteFlag가 1이 아닌 클럽만 가져오는 기본 메서드
//    List<Club> findByDeleteFlagFalse();
    
    // 페이지네이션을 적용한 목록 조회
    @Query("SELECT c FROM Club c LEFT JOIN Club_Member_List cm ON c.clubCode = cm.clubCode AND cm.deleteFlag = false "
    		+ "WHERE c.deleteFlag = false GROUP BY c.clubCode "
    		+ "ORDER BY COUNT(cm.memId) DESC ")
    Page<Club> findAllActiveClubs(Pageable pageable);
     
    // 테마별로 deleteFlag가 1이 아닌 클럽만 조회
    @Query("SELECT c FROM Club c LEFT JOIN Club_Member_List cm ON c.clubCode = cm.clubCode AND cm.deleteFlag = false "
    		+ "WHERE c.deleteFlag = false AND c.clubTheme LIKE %:clubTheme% "
    		+ "GROUP BY c.clubCode ORDER BY COUNT(cm.memId) DESC ")
    Page<Club> findByClubThemeContaining(@Param("clubTheme") String clubTheme, Pageable pageable);
    
    // 주소기반, 테마별 deleteFlag가 1이 아닌 클럽만 조회
    @Query("SELECT c FROM Club c LEFT JOIN Club_Member_List cm ON c.clubCode = cm.clubCode AND cm.deleteFlag = false "
    		+ "WHERE c.deleteFlag = false "
    		+ "AND (:address IS NULL OR c.clubAddress LIKE %:address%) "
    		+ "AND (:clubTheme IS NULL OR c.clubTheme LIKE %:clubTheme%) "
    		+ "GROUP BY c.clubCode ORDER BY COUNT(cm.memId) DESC ")
    Page<Club> searchAll(@Param("address") String address, @Param("clubTheme") String clubTheme, Pageable pageable);
    
    // 클럽 방장인지 체크
    Long countByMemIdAndDeleteFlag(String memId, Boolean deleteFlag);

    @Query("SELECT c FROM Club c WHERE c.memId = :memId AND c.deleteFlag = false ")
    List<Club> findByMemId(@Param("memId") String username);

    @Query(value = "select c.* from club c inner join club_member_list cml on c.CLUB_CODE = cml.CLUB_CODE where cml.mem_id =:memId and cml.DELETE_FLAG is false and c.DELETE_FLAG is false", nativeQuery = true)
    List<Club> getClubListWithMemID(@Param("memId") String memId);
    
    @Query("SELECT c.clubName FROM Club c WHERE c.clubCode = :clubCode")
    String findClubNameByClubCode(@Param("clubCode") String clubCode);
    
    // clubCode로 clubName 가져오기
    @Query("SELECT c.clubCode, c.clubName FROM Club c WHERE c.clubCode IN :clubCodes")
    List<Object[]> findClubNamesByClubCodes(@Param("clubCodes") List<String> clubCodes);

}
