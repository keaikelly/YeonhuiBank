package com.db.bank.app.dto;

import com.db.bank.domain.enums.transaction.TransactionStatus;
import com.db.bank.domain.enums.transaction.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {

    // ================================
    // 1) 거래 생성 요청 DTO (입금/출금/이체)
    // ================================
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {

        private String fromAccountNum;   // 출금 계좌 (입금 시 ExternalIn 가능)
        private String toAccountNum;     // 입금 계좌 (출금 시 ExternalOut 가능)
        private BigDecimal amount;
        private String memo;
    }


    // ================================
    // 2) 거래 응답 DTO
    // ================================
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long transactionId;
        private Long fromAccountId;
        private Long toAccountId;
        private TransactionType type;
        private TransactionStatus status;
        private BigDecimal amount;
        private String memo;
        private LocalDateTime createdAt;
    }
}
