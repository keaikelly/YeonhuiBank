package com.db.bank.app.dto;

import com.db.bank.domain.enums.transferLimit.TransferStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferLimitDto {

    /**
     * 이체한도 생성 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String accountNum;        // 대상 계좌번호
        private BigDecimal dailyLimitAmt; // 1일 이체 한도
        private BigDecimal perTxLimitAmt; // 1회 이체 한도
        private String note;              // 비고(메모)
    }

    /**
     * 공통 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long limitId;

        private String accountNum;

        private BigDecimal dailyLimitAmt;
        private BigDecimal perTxLimitAmt;

        private LocalDateTime startDate;
        private LocalDateTime endDate;

        private TransferStatus status;

        private String note;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateEndDateRequest {
        private LocalDateTime endDate;
    }
}
