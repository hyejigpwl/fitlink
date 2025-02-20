package com.lec.packages.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lec.packages.domain.Club_Board;
import com.lec.packages.domain.primaryKeyClasses.ClubBoardKeyClass;
import com.lec.packages.dto.ClubBoardAllListInterface;

public interface ClubBoardRepository extends JpaRepository<Club_Board, ClubBoardKeyClass>{

    @Query(value = "select * from club_board cb where cb.CLUB_CODE = :CLUB_CODE order by BOARD_NO desc limit 1", nativeQuery = true)
    Optional<Club_Board> findByClubCode(@Param("CLUB_CODE") String clubCode);

    @EntityGraph(attributePaths = {"images"})
	@Query("select cb from Club_Board cb where cb.clubCode = :clubCode and cb.boardNo = :boardNo")
	Optional<Club_Board> findBoardByImages(@Param("clubCode") String clubCode, @Param("boardNo") int boardNO);

    @EntityGraph(attributePaths = {"images"})
    // @Query("select cb from Club_Board cb where cb.memID =:memId order by cb.MODIFYDATE desc")
    List<Club_Board> findTop3ByMemIDOrderByMODIFYDATEDesc(String memId);

    @Query(value = "select count(cbr.reply_no) as replyCount, m.MEM_NICKNAME as memId, cb.* from club_board cb \r\n" + //
                "\t\tleft join club_board_reply cbr on cb.CLUB_CODE = cbr.club_code and cb.BOARD_NO = cbr.board_no \r\n" + //
                "\t\tleft join `member` m on cb.mem_id = m.mem_id\r\n" + //
                "\t\twhere cbr.delete_flag is null\r\n" + //
                "\t\tand cb.CLUB_CODE =:clubCode\r\n" + //
                "\t\tand cb.BOARD_TYPE = :types\r\n" + //
                "\t\tgroup by cb.CLUB_CODE, cb.BOARD_NO \r\n" + //
                "\t\torder by cb.MODIFYDATE desc", nativeQuery = true)
    Page<ClubBoardAllListInterface> searchWithAll(@Param("types") String types, Pageable pageable, @Param("clubCode") String clubCode);

    @Query(value = "select count(cbr.reply_no) as replyCount, m.MEM_NICKNAME as memId, cb.* from club_board cb \r\n" + //
                "\t\tleft join club_board_reply cbr on cb.CLUB_CODE = cbr.club_code and cb.BOARD_NO = cbr.board_no \r\n" + //
                "\t\tleft join `member` m on cb.mem_id = m.mem_id\r\n" + //
                "\t\twhere cbr.delete_flag is null\r\n" + //
                "\t\tand cb.CLUB_CODE =:clubCode\r\n" + //
                "\t\tgroup by cb.CLUB_CODE, cb.BOARD_NO \r\n" + //
                "\t\torder by cb.MODIFYDATE desc", nativeQuery = true)
    Page<ClubBoardAllListInterface> searchWithAll(Pageable pageable, @Param("clubCode") String clubCode);
}
