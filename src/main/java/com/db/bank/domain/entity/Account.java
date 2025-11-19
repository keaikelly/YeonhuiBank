package com.db.bank.domain.entity;


import com.db.bank.domain.enums.account.*;
import jakarta.persistence.*;
import lombok.*;
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
    private Long id;

    // 예: 123-456-789012
    @Column(nullable = false, unique = true, length = 30)
    private String accountNum;

    // 계좌 잔액
    @Column(nullable = false)
    private Long balance;

    // 계좌 타입 (체크카드, 예금 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    // 계좌 상태 (활성/해지 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    // 계좌 생성일
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 사용자와 FK (User 하나가 여러 계좌) 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
