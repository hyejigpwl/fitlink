package com.lec.packages.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMemberDTO {
	
    @NotEmpty
    private String clubCode;
    
    @NotEmpty
    private String memId;
    
    private Boolean deleteFlag;
    
    private LocalDateTime CREATEDATE;
    private LocalDateTime MODIFYDATE;

}
