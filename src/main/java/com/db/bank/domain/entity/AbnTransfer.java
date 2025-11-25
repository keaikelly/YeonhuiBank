package com.db.bank.domain.entity;

import com.db.bank.domain.enums.abnTransfer.RuleCode;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class AbnTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="alert_id")
    private Long alertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="transaction_id", nullable = false)
    private Transaction transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_num", nullable = false)
    private Account accountNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleCode ruleCode;

    @Column(name = "detail_message", length=255)
    private String detailMessage;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

}
