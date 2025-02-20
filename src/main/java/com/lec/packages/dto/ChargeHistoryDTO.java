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
public class ChargeHistoryDTO {
    private String chargeCode;          // Unique identifier for each charge
    private String memId;           // Member ID who made the charge
    private BigDecimal amount;      // Amount charged
    private LocalDateTime chargeDate; // Date and time of the charge
    private String paymentMethod;   // Payment method (e.g., card, transfer)
    private String status;          // Status of the charge (e.g., Success, Failed)
}
