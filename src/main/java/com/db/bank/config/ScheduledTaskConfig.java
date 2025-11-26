package com.db.bank.config;

import com.db.bank.service.ScheduledTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScheduledTaskConfig {

    private final ScheduledTransactionService scheduledTransactionService;
    /**
     * 1분마다 실행
     * 매 분마다 runDueSchedules()가 자동 실행
     * nextRunAt <= 현재시간 인 예약 이체들을 실행
     * 정상/실패 처리 → 시간 업데이트
     * endDate 지나면 COMPLETED 처리
    */
    @Scheduled(cron = "0 */1 * * * *")
    public void runScheduledTransactions() {
        scheduledTransactionService.runDueSchedules(LocalDateTime.now());
    }
}
