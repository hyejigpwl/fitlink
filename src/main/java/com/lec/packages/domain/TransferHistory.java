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
@Table(name = "TRANSFER_HISTORY")
public class TransferHistory {

    @Id
    @Column(name = "TRANSFER_CODE")
    private String transferCode;

    @Column(name = "PAY_CODE")
	 private String payCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_ID", referencedColumnName = "MEM_ID", nullable = false)
    private Member senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEIVER_ID", referencedColumnName = "MEM_ID", nullable = false)
    private Member receiverId;


    @Column(name = "AMOUNT", nullable = false, precision = 50, scale = 0)
    private BigDecimal amount;

    @Column(name = "TRANSFER_DATE", nullable = false)
    private LocalDateTime transferDate;

    // 송금 상태: 성공, 실패
    @Column(name = "STATUS", length = 20, nullable = false)
    private String status;

    @Column(name = "MEMO", columnDefinition = "TEXT")
    private String memo;
    
    @Column(length = 100, name = "CLUB_CODE")
    private String clubCode;
}
