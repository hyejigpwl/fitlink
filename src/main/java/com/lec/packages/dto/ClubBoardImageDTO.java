package com.lec.packages.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubBoardImageDTO {
    
    private String uuid;

    private String boardImage;

    private int ord;
}
