package com.db.bank.domain.entity;

import com.db.bank.domain.enums.transferLimit.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "transfer_limit")

public class TransferLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name="limit_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_num", nullable = false)
    private Account account;

    @Column(name = "daily_limit_amt", nullable = true, precision = 18, scale = 2)
    private BigDecimal dailyLimitAmt;

    @Column(name = "per_tx_limit_amt", nullable = true, precision = 18, scale = 2)
    private BigDecimal perTxLimitAmt;

    @Column(name="start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name="end_date", nullable = true)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column(nullable = true, length = 255)
    private String note;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

}

