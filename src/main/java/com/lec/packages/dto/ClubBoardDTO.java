package com.lec.packages.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubBoardDTO {
    
    @NotEmpty
    private String CLUB_CODE;

    private int BOARD_NO;
    
    private String BOARD_TYPE;

    private String BOARD_TEXT;

    private String MEM_ID;

    private Boolean DELETE_FLAG;

    private List<String> fileNames;

    private LocalDateTime CREATEDATE;
    
    private LocalDateTime MODIFYDATE;
}
