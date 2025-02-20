package com.lec.packages.dto;

import java.time.LocalDateTime;
import java.util.List;

public interface ClubBoardAllListInterface {
    int getReplyCount();
    String getMemId();
    int getBoardNo();
    String getBoardType();
    String getBoardText();
    LocalDateTime getMODIFYDATE();
    List<ClubBoardImageDTO> getBoardImages();
    
}
