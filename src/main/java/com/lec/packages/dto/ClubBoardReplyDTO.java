package com.lec.packages.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubBoardReplyDTO {

    private int replyNo;

    private int boardNo;

    private String clubCode;

    private String boardText;

    private String memId;
}
