package com.db.bank.config;

import com.db.bank.apiPayload.exception.TransactionException;
import com.db.bank.domain.entity.ScheduledTransferRun;
import com.db.bank.domain.entity.TransferFailureReason;
import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import com.db.bank.service.ScheduledTransactionService;
import com.db.bank.service.ScheduledTransferRunService;
import com.db.bank.service.TransactionService;
import com.db.bank.service.TransferFailureReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountLockedException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduledTaskConfig {

    private final ScheduledTransactionService scheduledTransactionService;
    private final ScheduledTransferRunService scheduledTransferRunService;
    private final TransactionService transactionService;
    private final TransferFailureReasonService failureReasonService;

    /**
     * 1) 예약이체 정상 실행
     * - next_run_at <= now 인 스케줄 실행
     */
    @Scheduled(cron = "0 */1 * * * *")   // 매 1분마다 0초에
    public void runScheduledTransactions() {
        LocalDateTime now = LocalDateTime.now();
        scheduledTransactionService.runDueSchedules(now);
    }

    /**
     * 2) 실패한 예약이체 재시도
     * - retry_no < maxRetries
     * - next_retry_at <= now
     */
    @Scheduled(cron = "30 */1 * * * *")  // 매 5분마다 30초에
    public void retryFailedScheduledTransfers() {

        LocalDateTime now = LocalDateTime.now();

        // 1. 재시도 대상 조회 (예: retry_no < 3, next_retry_at <= now)
        List<ScheduledTransferRun> retryTargets =
                scheduledTransferRunService.getRetryTargets(now, 3);

        for (ScheduledTransferRun run : retryTargets) {

            var schedule = run.getSchedule();

            try {
                // 2. 다시 이체 시도
                var resultTx = transactionService.transfer(
                        schedule.getCreatedBy().getId(),
                        schedule.getFromAccount().getAccountNum(),
                        schedule.getToAccount().getAccountNum(),
                        schedule.getAmount(),
                        "[재시도] " + schedule.getMemo()
                );

                // 3. 성공 시: 성공 로그 기록
                scheduledTransferRunService.recordSuccess(
                        schedule,
                        resultTx,
                        null,
                        "재시도 성공"
                );

            } catch (Exception e) {
                // 4. 실패 시: 실패 사유 코드 분기 + 재시도 횟수 증가

                int nextRetry = run.getRetryNo() + 1;
                LocalDateTime nextRetryAt = now.plusMinutes(10);

                // 실패 사유 코드 매핑 로직
                TransferFailureReason reason;

                if (e instanceof TransactionException.InsufficientFundsException) {
                    // 잔액 부족
                    reason = failureReasonService.getReason("INSUFFICIENT_FUNDS");

                } else if (e instanceof AccountLockedException) {
                    // 계좌 잠김
                    reason = failureReasonService.getReason("ACCOUNT_LOCKED");

                } else if (e instanceof TransactionException.DailyLimitExceededException) {
                    // 일일 한도 초과
                    reason = failureReasonService.getReason("DAILY_LIMIT_EXCEEDED");

                } else {
                    // 기타 알 수 없는/예상치 못한 실패 → 재시도 실패로 통일
                    reason = failureReasonService.getReason("RETRY_FAILED");
                }
                // --- 🔺 실패 사유 코드 매핑 끝 🔺 ---

                // 5. 최대 재시도 초과 여부에 따라 RunResult 분기 (선택사항)
                RunResult resultEnum;
                if (nextRetry > run.getMaxRetries()) {
                    // 더 이상 재시도 안 함 → SKIPPED(또는 FINAL_FAILED 등으로 정의 가능)
                    resultEnum = RunResult.SKIPPED;
                    nextRetryAt = null;  // 더 이상 재시도 예정 없음
                } else {
                    // 아직 재시도 여지 있음 → ERROR
                    resultEnum = RunResult.ERROR;
                }

                // 6. 실패 로그 기록
                scheduledTransferRunService.recordFailure(
                        run.getSchedule(),
                        run.getTxnOut(),   // 직전 실행에서 사용한 txnOut/txnIn 재사용 (필요 시 null 처리 가능)
                        run.getTxnIn(),
                        resultEnum,
                        "재시도 실패: " + e.getMessage(),
                        reason,
                        nextRetry,
                        run.getMaxRetries(),
                        nextRetryAt
                );
            }
        }
    }
}
