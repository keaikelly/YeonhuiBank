package com.db.bank.domain.entity;


import com.db.bank.domain.enums.account.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    // 예: 123-456-789012
    @Column(nullable = false, unique = true, length = 30)
    private String accountNum;

    // 계좌 잔액 최초 0
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // 기본값 NORMAL (일반 사용자 계좌)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType = AccountType.NORMAL;


    // 계좌 생성일
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 사용자와 FK (User 하나가 여러 계좌) 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
