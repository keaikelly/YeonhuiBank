package com.db.bank.app.dto;
import com.db.bank.domain.enums.log.Action;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public class LogDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogResponse {

        private Long logId;

        private Long transactionId;

        private String accountNum;

        private Long actorUserId;

        private BigDecimal beforeBalance;

        private BigDecimal afterBalance;

        private Action action;

        private LocalDateTime createdAt;
    }
}
