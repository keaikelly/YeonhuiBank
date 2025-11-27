package com.db.bank.app.dto;


import com.db.bank.domain.enums.account.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDto {

    // ---------- 계좌 생성 요청 ----------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        private String accountNum;
        private AccountType accountType;
        private BigDecimal initialBalance;
    }

    // ---------- 계좌 생성 응답 ----------
    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateResponse {
        private Long accountId;
        private String accountNum;
        private AccountType accountType;
        private BigDecimal balance;
        private LocalDateTime createdAt;
        private Long userId;
    }

    // ---------- 단일 계좌 조회 응답 ----------
    @Getter
    @Builder
    @AllArgsConstructor
    public static class DetailResponse {
        private Long accountId;
        private String accountNum;
        private AccountType accountType;
        private BigDecimal balance;
        private LocalDateTime createdAt;
        private Long userId;
    }
}
