package com.db.bank.app.dto;

import com.db.bank.domain.enums.abnTransfer.RuleCode;
import lombok.*;

import java.time.LocalDateTime;

public class AbnTransferDto {

    // 이상거래 수동 등록용 요청 DTO (관리자용)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long transactionId;   // 대상 거래 ID
        private String accountNum;    // 이상거래 기준 계좌번호 (보낸 쪽 등)
        private RuleCode ruleCode;    // 룰 코드
        private String detailMessage; // 상세 메시지
    }

    // 공통 응답 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long alertId;

        private Long transactionId;
        private String accountNum;

        private RuleCode ruleCode;
        private String detailMessage;

        private LocalDateTime createdAt;
    }
}
