package com.db.bank.service;

import com.db.bank.apiPayload.exception.AccountException;
import com.db.bank.apiPayload.exception.ScheduledTransactionException;
import com.db.bank.apiPayload.exception.TransactionException;
import com.db.bank.apiPayload.exception.UserException;
import com.db.bank.domain.entity.*;
import com.db.bank.domain.enums.scheduledTransaction.Frequency;
import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import com.db.bank.domain.enums.scheduledTransaction.ScheduledStatus;
import com.db.bank.repository.AccountRepository;
import com.db.bank.repository.ScheduledTransactionRepository;
import com.db.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTransactionService {

    private final ScheduledTransactionRepository scheduledTransactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final ScheduledTransferRunService scheduledTransferRunService;
    private final TransferFailureReasonService failureReasonService;
    // ================== 1. 예약이체 생성 ==================

    @Transactional
    public ScheduledTransaction createSchedule(
            Long userId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime runTime,
            String rruleString,
            String memo
    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ScheduledTransactionException.InvalidScheduledTransactionAmountException("예약이체 금액은 0보다 커야 합니다.");
        }
        if (startDate == null) {
            throw new ScheduledTransactionException.InvalidScheduledTransactionStartDateException("시작일은 필수입니다.");
            //startDate = LocalDate.now();
        }
        if (runTime == null) {
            runTime = LocalTime.of(9, 30);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNonExistsException("사용자를 찾을 수 없습니다. id=" + userId));

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("출금 계좌를 찾을 수 없습니다. id=" + fromAccountId));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("입금 계좌를 찾을 수 없습니다. id=" + toAccountId));

        // 출금 계좌 소유자 검증
        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 출금 계좌는 이 사용자의 계좌가 아닙니다.");
        }

        // 중복 예약이체 존재 여부 체크
        boolean exists = scheduledTransactionRepository
                .existsByFromAccountIdAndToAccountIdAndScheduledStatus(fromAccountId, toAccountId, ScheduledStatus.ACTIVE);
        if (exists) {

            throw new ScheduledTransactionException.ScheduledTransactionAlreadyExistsException("동일한 출금/입금 계좌로 이미 활성화된 예약이체가 존재합니다.");
        }

        LocalDateTime firstRunAt = startDate.atTime(runTime);

        ScheduledTransaction schedule = ScheduledTransaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .createdBy(user)
                .amount(amount)
                .scheduledStatus(ScheduledStatus.ACTIVE)
                .frequency(frequency)
                .startDate(startDate)
                .endDate(endDate)
                .runTime(runTime)
                .nextRunAt(firstRunAt)
                .lastRunAt(null)
                .memo(memo)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .rrule(rruleString)
                .build();

        return scheduledTransactionRepository.save(schedule);
    }


    // ================== 2. 사용자용 조회 ==================

    // 2-1. 내가 만든 예약이체 목록 (최신순)
    @Transactional(readOnly = true)
    public Page<ScheduledTransaction> getMySchedules(Long userId, Pageable pageable) {
        return scheduledTransactionRepository.findByCreatedByIdOrderByCreatedAtDesc(userId, pageable);
    }

    // 2-2. 내가 만든 예약이체 + 상태 필터 (ACTIVE, CANCELED 등)
    @Transactional(readOnly = true)
    public Page<ScheduledTransaction> getMySchedulesByStatus(
            Long userId,
            ScheduledStatus scheduledStatus,
            Pageable pageable
    ) {
        return scheduledTransactionRepository.findByCreatedByIdAndScheduledStatusOrderByCreatedAtDesc(
                userId,
                scheduledStatus,
                pageable
        );
    }

    // 2-3. 특정 출금 계좌 기준 예약이체 목록
    @Transactional(readOnly = true)
    public Page<ScheduledTransaction> getSchedulesByFromAccount(
            Long fromAccountId,
            Pageable pageable
    ) {
        return scheduledTransactionRepository.findByFromAccountIdOrderByCreatedAtDesc(
                fromAccountId,
                pageable
        );
    }

    // 2-4. 예약이체 단건 상세 조회 (소유자 검증 포함)
    @Transactional(readOnly = true)
    public ScheduledTransaction getScheduleDetail(Long userId, Long scheduleId) {
        ScheduledTransaction schedule = scheduledTransactionRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduledTransactionException.ScheduledTransactionNotFoundException("예약이체를 찾을 수 없습니다. id=" + scheduleId));

        if (!schedule.getCreatedBy().getId().equals(userId)) {
            throw new ScheduledTransactionException.UnauthorizedScheduledTransaction("해당 예약이체에 접근 권한이 없습니다.");
        }

        return schedule;
    }

    // ================== 3. 수정 / 상태 변경 ==================

    // 3-1. 예약이체 수정 (금액/날짜/빈도/메모 등)
    @Transactional
    public ScheduledTransaction updateSchedule(
            Long userId,
            Long scheduleId,
            BigDecimal amount,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            String memo
    ) {
        ScheduledTransaction schedule = getScheduleDetail(userId, scheduleId);

        // 상태 검사 (완료/취소된 건 수정 불가 등 정책)
        if (schedule.getScheduledStatus() == ScheduledStatus.CANCELED || schedule.getScheduledStatus() == ScheduledStatus.COMPLETED) {
            throw new ScheduledTransactionException.ScheduledTransactionAlreadyFinishedException("이미 종료된 예약이체는 수정할 수 없습니다.");
        }

        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ScheduledTransactionException.InvalidScheduledTransactionAmountException("예약이체 금액은 0보다 커야 합니다.");
            }
            schedule.setAmount(amount);
        }

        if (frequency != null) {
            schedule.setFrequency(frequency);
        }

        if (startDate != null) {
            schedule.setStartDate(startDate);
        }

        if (endDate != null) {
            schedule.setEndDate(endDate);
        }

        if (memo != null) {
            schedule.setMemo(memo);
        }

        // nextRunAt 재계산 (정책에 따라 startDate 기준 or 현재 기준)
        schedule.setNextRunAt(recalculateNextRunAt(schedule));
        schedule.setUpdatedAt(LocalDateTime.now());

        return schedule; // @Transactional 덕분에 flush 시점에 자동 업데이트
    }

    // 3-2. 취소
    @Transactional
    public void cancelSchedule(Long userId, Long scheduleId) {
        ScheduledTransaction schedule = getScheduleDetail(userId, scheduleId);

        if (schedule.getScheduledStatus() == ScheduledStatus.CANCELED) {
            return; // 이미 취소된 경우 그냥 무시
        }

        schedule.setScheduledStatus(ScheduledStatus.CANCELED);
        schedule.setNextRunAt(null);
        schedule.setUpdatedAt(LocalDateTime.now());
    }

    // 3-3. 일시정지
    @Transactional
    public void pauseSchedule(Long userId, Long scheduleId) {
        ScheduledTransaction schedule = getScheduleDetail(userId, scheduleId);

        if (schedule.getScheduledStatus() != ScheduledStatus.ACTIVE) {
            throw new ScheduledTransactionException.InvalidScheduleStatusForPauseException("ACTIVE 상태의 예약이체만 일시정지할 수 있습니다.");
        }

        schedule.setScheduledStatus(ScheduledStatus.PAUSED);
        schedule.setUpdatedAt(LocalDateTime.now());
    }

    // 3-4. 재개
    @Transactional
    public void resumeSchedule(Long userId, Long scheduleId) {
        ScheduledTransaction schedule = getScheduleDetail(userId, scheduleId);

        if (schedule.getScheduledStatus() != ScheduledStatus.PAUSED) {
            throw new ScheduledTransactionException.InvalidScheduleStatusForResumeException("PAUSED 상태의 예약이체만 재개할 수 있습니다.");
        }

        schedule.setScheduledStatus(ScheduledStatus.ACTIVE);
        schedule.setNextRunAt(recalculateNextRunAt(schedule));
        schedule.setUpdatedAt(LocalDateTime.now());
    }

    // ================== 4. 배치/스케줄러용 실행 로직 ==================

    /**
     * 지금 기준으로 실행해야 할 예약이체들을 조회해서 처리하는 메서드
     * - 보통 @Scheduled(cron = "...") 로 스케줄링해서 호출
     */
    @Transactional
    public void runDueSchedules(LocalDateTime now) {
        List<ScheduledTransaction> dueList =
                scheduledTransactionRepository.findByScheduledStatusAndNextRunAtLessThanEqual(
                        ScheduledStatus.ACTIVE,
                        now
                );

        for (ScheduledTransaction schedule : dueList) {
            executeSchedule(schedule, now);
        }
    }

    @Transactional
    public void runNow(Long userId, Long scheduleId) {
        // 1. 예약이체 소유자 + 존재 여부 검증
        ScheduledTransaction schedule = getScheduleDetail(userId, scheduleId);

        // 2. 상태 검사 (원하면 ACTIVE일 때만 허용)
        if (schedule.getScheduledStatus() != ScheduledStatus.ACTIVE) {
            throw new ScheduledTransactionException.InvalidScheduleStatusForPauseException(
                    "ACTIVE 상태의 예약이체만 즉시 실행할 수 있습니다."
            );
        }

        // 3. 지금 시각 기준으로 한 번 실행
        LocalDateTime now = LocalDateTime.now();
        executeSchedule(schedule, now);
    }

    /**
     * 예약이체 1건을 지금(now) 기준으로 실행하는 공통 로직
     * - runDueSchedules / runNow 둘 다 여기로 모아서 사용
     */
    @Transactional
    protected void executeSchedule(ScheduledTransaction schedule, LocalDateTime now) {
        try {
            // 1) 실제 계좌 이체
            Transaction tx = transactionService.transfer(
                    schedule.getCreatedBy().getId(),
                    schedule.getFromAccount().getAccountNum(),
                    schedule.getToAccount().getAccountNum(),
                    schedule.getAmount(),
                    schedule.getMemo()
            );

            // 2) 성공 실행 로그 기록
            scheduledTransferRunService.recordSuccess(
                    schedule,
                    tx,
                    null,                   // externalAccountNum 등 필요하면 나중에 추가
                    "예약이체 성공"
            );

            // 3) 실행 시간 갱신 + 다음 실행 시각 계산
            schedule.setLastRunAt(now);
            schedule.setNextRunAt(recalculateNextRunAt(schedule));

        }catch (AccountException.InsufficientBalanceException e) {

            int retryNo = 0;
            int maxRetries = 3;
            LocalDateTime nextRetryAt = now.plusMinutes(10);

            // 실패 사유 코드: INSUFFICIENT_FUNDS (DB에 미리 만들어둔 코드)
            TransferFailureReason reason = failureReasonService.getReason("INSUFFICIENT_FUNDS");
            System.out.println("[디버그]failureRecord");
            scheduledTransferRunService.recordFailure(
                    schedule,
                    null,
                    null,
                    RunResult.ERROR,
                    "예약이체 실패: " + e.getMessage(),
                    reason,
                    retryNo,
                    maxRetries,
                    nextRetryAt
            );

            schedule.setLastRunAt(now);
            schedule.setNextRunAt(nextRetryAt);

            // ❗지금처럼 409 응답을 유지하고 싶으면 다시 던져줌
            throw e;

            // 🔹 그 외 우리가 정의한 TransactionException 처리 (한도 초과 등)
        }  catch (TransactionException e) {
            // 4) 실패 케이스 처리 (처음 실패 기준으로 retryNo=0)
            int retryNo = 0;
            int maxRetries = 3;
            LocalDateTime nextRetryAt = now.plusMinutes(10);

            // 실패 사유 코드 매핑
            TransferFailureReason reason;
            if (e instanceof TransactionException.InsufficientFundsException) {
                reason = failureReasonService.getReason("INSUFFICIENT_FUNDS");
            } else if (e instanceof TransactionException.AccountLockedException) {
                reason = failureReasonService.getReason("ACCOUNT_LOCKED");
            } else if (e instanceof TransactionException.DailyLimitExceededException) {
                reason = failureReasonService.getReason("DAILY_LIMIT_EXCEEDED");
            } else {
                reason = failureReasonService.getReason("RETRY_FAILED");
            }

            // 실패 실행 로그 기록
            scheduledTransferRunService.recordFailure(
                    schedule,
                    null,
                    null,
                    RunResult.ERROR,
                    "예약이체 실패: " + e.getMessage(),
                    reason,
                    retryNo,
                    maxRetries,
                    nextRetryAt
            );

            // 스케줄에 실패/다음 재시도 시간 반영
            schedule.setLastRunAt(now);
            schedule.setNextRunAt(nextRetryAt);
        }
    }





    // ================== 5. nextRunAt 계산 헬퍼 ==================


    private LocalDateTime recalculateNextRunAt(ScheduledTransaction schedule) {
        // lastRunAt가 있으면 그 기준으로, 없으면 startDate + runTime 기준
        LocalDateTime base = schedule.getLastRunAt();
        if (base == null) {
            // ⭐ 하드코딩 9시 대신, 엔티티에 저장해 둔 runTime 사용
            LocalTime runTime = schedule.getRunTime();
            if (runTime == null) {
                // 혹시 null이면 기본값은 09:00으로 (방어 코드)
                runTime = LocalTime.of(9, 0);
            }
            base = LocalDateTime.of(schedule.getStartDate(), runTime);
        }
        return calculateNextRunAt(schedule.getFrequency(), schedule.getRrule(), base);
    }

    private LocalDateTime calculateNextRunAtCustom(String rrule, LocalDateTime base) {
        if (rrule == null || rrule.isBlank()) return null;

        // "FREQ=WEEKLY;INTERVAL=2" 같은 문자열 파싱
        String[] parts = rrule.split(";");
        String freq = null;
        Integer interval = null;
        Integer byMonthDay = null;

        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length != 2) continue;
            String key = kv[0].trim().toUpperCase();
            String value = kv[1].trim().toUpperCase();

            switch (key) {
                case "FREQ":
                    freq = value; // DAILY, WEEKLY, MONTHLY 등
                    break;
                case "INTERVAL":
                    interval = Integer.parseInt(value); // 1,2,3...
                    break;
                case "BYMONTHDAY":
                    byMonthDay = Integer.parseInt(value); // 1~31
                    break;
            }
        }

        if (freq == null) return null;
        if (interval == null) interval = 1;

        switch (freq) {
            case "DAILY":
                return base.plusDays(interval);
            case "WEEKLY":
                return base.plusWeeks(interval);
            case "MONTHLY":
                if (byMonthDay != null) {
                    // 다음 달 같은 시간, day만 BYMONTHDAY로 맞추기
                    LocalDateTime nextMonth = base.plusMonths(interval);
                    int lastDayOfMonth = nextMonth.toLocalDate().lengthOfMonth();
                    int day = Math.min(byMonthDay, lastDayOfMonth);
                    return LocalDateTime.of(
                            nextMonth.getYear(),
                            nextMonth.getMonth(),
                            day,
                            base.getHour(),
                            base.getMinute()
                    );
                } else {
                    return base.plusMonths(interval);
                }
            default:
                return null;
        }
    }

    private LocalDateTime calculateNextRunAt(Frequency frequency, String rrule,LocalDateTime base) {
        if (frequency == null) return null;

        switch (frequency) {
            case ONCE:
                // 한 번만 실행이면 다음 실행 없음
                return null;
            case DAILY:
                return base.plusDays(1);
            case WEEKLY:
                return base.plusWeeks(1);
            case MONTHLY:
                return base.plusMonths(1);
            case CUSTOM:
                return calculateNextRunAtCustom(rrule, base);
            default:
                return null;

        }
    }
}
