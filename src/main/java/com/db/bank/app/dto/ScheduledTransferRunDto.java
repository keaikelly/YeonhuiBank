package com.db.bank.app.dto;

import com.db.bank.domain.entity.TransferFailureReason;
import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import lombok.*;

import java.time.LocalDateTime;

public class ScheduledTransferRunDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduledTransferRunResponse {
        private Long runId;

        private Long scheduleId;

        private LocalDateTime executedAt;

        private RunResult result;

        private String message;

        private Long txnOutId;
        private Long txnInId;

        private TransferFailureReason failureReason;

        private int retryNo;
        private int maxRetries;

        private LocalDateTime nextRetryAt;
    }
    @Getter
    @Builder
    public static class FailureResponse {
        private Long runId;
        private Long scheduleId;
        private LocalDateTime executedAt;
        private String runTime;              // "09:30:00" 같은 문자열로 줘도 됨
        private RunResult result;            // ERROR, INSUFFICIENT_FUNDS 등
        private String message;              // 실패 메시지 (잘린 상태)
        private String failureReasonCode;    // INSUFFICIENT_FUNDS
        private String failureReasonDesc;    // "잔액 부족"
        private Integer retryNo;
        private Integer maxRetries;
        private LocalDateTime nextRetryAt;
    }
}
