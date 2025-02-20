package com.lec.packages.domain;

import com.lec.packages.domain.primaryKeyClasses.ClubBoardReplyKeyClass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
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
@NoArgsConstructor
@AllArgsConstructor
@ToString
@IdClass(ClubBoardReplyKeyClass.class)
public class Club_Board_Reply extends BaseEntity{

    @Id
    @Column(name = "REPLY_NO", nullable = false)
    private int replyNo;

    @Column(name = "BOARD_TEXT", columnDefinition = "TEXT")
    private String boardText;

    @JoinColumn(name = "MEM_ID", nullable = false)
    @Column(name = "MEM_ID", length = 100)
    private String memId;

    @Column(name = "DELETE_FLAG")
    private Boolean deleteFlag;

    @Id
    @JoinColumn(name = "CLUB_CODE", table = "Club_Board")
    @Column(name = "CLUB_CODE", nullable = false, length = 10)
    private String clubCode;

    @Id
    @JoinColumn(name = "BOARD_NO", table = "Club_Board")
    @Column(name = "BOARD_NO", nullable = false)
    private int boardNo;

    public void changeText(String replyText) {
        this.boardText = replyText;
    }
}
