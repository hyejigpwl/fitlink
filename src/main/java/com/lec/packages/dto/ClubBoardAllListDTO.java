package com.lec.packages.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubBoardAllListDTO {

    private int boardNo;
    private String type;
    private String memId;
    private String boardText;
    private long modDate;
    private int replyCount;
    private List<ClubBoardImageDTO> boardImages;
}
