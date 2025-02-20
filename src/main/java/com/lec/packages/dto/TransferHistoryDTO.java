package com.lec.packages.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferHistoryDTO {
    private String transferCode; 
    private String payCode;
    private String senderId;          
    private String receiverId;        
    private BigDecimal amount;        
    private LocalDateTime transferDate;
    private String status;            
    private String memo;              
    private String clubCode;
}
