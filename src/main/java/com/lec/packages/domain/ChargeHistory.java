package com.lec.packages.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "CHARGE_HISTORY")
public class ChargeHistory {

    @Id
    @Column(length = 20, name = "CHARGE_CODE")
    private String chargeCode;

    @Column(name = "MEM_ID", nullable = false)
    private String memId;

    @Column(name = "AMOUNT", nullable = false,  precision = 50, scale = 0)
    private BigDecimal amount;

    @Column(name = "CHARGE_DATE", nullable = false)
    private LocalDateTime chargeDate;

    @Column(name = "PAYMENT_METHOD", length = 50, nullable = false)
    private String paymentMethod;

    // 결제상태 : 성공, 실패
    @Column(name = "STATUS", length = 20, nullable = false)
    private String status;
}
