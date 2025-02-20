package com.lec.packages.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lec.packages.domain.Club_Board_Reply;
import com.lec.packages.domain.primaryKeyClasses.ClubBoardReplyKeyClass;

public interface ClubBoardReplyRepository extends JpaRepository<Club_Board_Reply, ClubBoardReplyKeyClass>{
    @Query(value = "select * from club_board_reply sbr where sbr.club_code = :clubCode and sbr.board_no = :boardNo and sbr.delete_flag IS NULL order by sbr.reply_no desc limit 1", nativeQuery = true)
    Optional<Club_Board_Reply> findReplyNoByKey(@Param("clubCode") String clubCode, @Param("boardNo") int boardNO);

    @Query("select sbr from Club_Board_Reply sbr where sbr.boardNo = :boardNo and sbr.clubCode = :clubCode and sbr.deleteFlag is null")
    Page<Club_Board_Reply> listOfReply(@Param("boardNo") int boardNo, @Param("clubCode") String clubCode, Pageable pageable);
}
