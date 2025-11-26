package com.db.bank.app.dto;

import com.db.bank.domain.enums.scheduledTransaction.Frequency;
import com.db.bank.domain.enums.scheduledTransaction.ScheduledStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ScheduledTransactionDto {

    // ==================================
    // 1) 예약이체 생성 요청 DTO
    // ==================================
    @Getter @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long userId;
        private Long fromAccountId;
        private Long toAccountId;
        private BigDecimal amount;
        private Frequency frequency;

        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime runTime;

        private String rrule;
        private String memo;
    }

    // ==================================
    // 2) 예약이체 수정 요청 DTO
    // ==================================
    @Getter @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private BigDecimal amount;
        private Frequency frequency;
        private LocalDate startDate;
        private LocalDate endDate;
        private String memo;
    }

    // ==================================
    // 3) 조회 응답 DTO
    // ==================================
    @Getter @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long scheduleId;

        private Long fromAccountId;
        private Long toAccountId;

        private Long createdBy;

        private BigDecimal amount;
        private ScheduledStatus scheduledStatus;
        private Frequency frequency;
        private String rrule;

        private LocalDate startDate;
        private LocalDate endDate;

        private LocalDateTime nextRunAt;
        private LocalDateTime lastRunAt;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private LocalTime runTime;

        private String memo;
    }
}
