package com.db.bank.app.dto;

import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import lombok.*;

import java.time.LocalDateTime;

public class ScheduledTransferRunDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long runId;

        private Long scheduleId;

        private LocalDateTime executedAt;

        private RunResult result;

        private String message;

        private Long txnOutId;
        private Long txnInId;

        private String failureReasonCode;

        private int retryNo;
        private int maxRetries;

        private LocalDateTime nextRetryAt;
    }
}
