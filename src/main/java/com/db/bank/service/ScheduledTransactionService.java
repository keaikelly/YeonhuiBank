package com.db.bank.service;

import com.db.bank.apiPayload.exception.AccountException;
import com.db.bank.apiPayload.exception.ScheduledTransactionException;
import com.db.bank.apiPayload.exception.UserException;
import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.ScheduledTransaction;
import com.db.bank.domain.entity.User;
import com.db.bank.domain.enums.scheduledTransaction.Frequency;
import com.db.bank.domain.enums.scheduledTransaction.Status;
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
            String memo
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ScheduledTransactionException.InvalidScheduledTransactionAmountException("예약이체 금액은 0보다 커야 합니다.");
        }
        if (startDate == null) {
            throw new ScheduledTransactionException.InvalidScheduledTransactionStartDateException("시작일은 필수입니다.");
        }
        if (runTime == null) {
            throw new ScheduledTransactionException.InvalidScheduledTransactionTimeException("실행 시간(runTime)은 필수입니다.");
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
                .existsByFromAccountIdAndToAccountIdAndStatus(fromAccountId, toAccountId, Status.ACTIVE);
        if (exists) {

            throw new ScheduledTransactionException.ScheduledTransactionAlreadyExistsException("동일한 출금/입금 계좌로 이미 활성화된 예약이체가 존재합니다.");
        }

        LocalDateTime firstRunAt = startDate.atTime(runTime);

        ScheduledTransaction schedule = ScheduledTransaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .createdBy(user)
                .amount(amount)
                .status(Status.ACTIVE)
                .frequency(frequency)
                .startDate(startDate)
                .endDate(endDate)
                .nextRunAt(firstRunAt)
                .lastRunAt(null)
                .memo(memo)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
            Status status,
            Pageable pageable
    ) {
        return scheduledTransactionRepository.findByCreatedByIdAndStatusOrderByCreatedAtDesc(
                userId,
                status,
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
        if (schedule.getStatus() == Status.CANCELED || schedule.getStatus() == Status.COMPLETED) {
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

        if (schedule.getStatus() == Status.CANCELED) {
            return; // 이미 취소된 경우 그냥 무시
        }

        schedule.setStatus(Status.CANCELED);
        schedule.setNextRunAt(null);
        schedule.setUpdatedAt(LocalDateTime.now());
    }

    // 3-3. 일시정지
    @Transactional
    public void pauseSchedule(Long userId, Long scheduleId) {
        ScheduledTransaction schedule = getScheduleDetail(userId, scheduleId);

        if (schedule.getStatus() != Status.ACTIVE) {
            throw new ScheduledTransactionException.InvalidScheduleStatusForPauseException("ACTIVE 상태의 예약이체만 일시정지할 수 있습니다.");
        }

        schedule.setStatus(Status.PAUSED);
        schedule.setUpdatedAt(LocalDateTime.now());
    }

    // 3-4. 재개
    @Transactional
    public void resumeSchedule(Long userId, Long scheduleId) {
        ScheduledTransaction schedule = getScheduleDetail(userId, scheduleId);

        if (schedule.getStatus() != Status.PAUSED) {
            throw new ScheduledTransactionException.InvalidScheduleStatusForResumeException("PAUSED 상태의 예약이체만 재개할 수 있습니다.");
        }

        schedule.setStatus(Status.ACTIVE);
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
        // ACTIVE + nextRunAt <= now 인 예약이체 중 최대 100개
        List<ScheduledTransaction> dueList =
                scheduledTransactionRepository.findTop100ByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(
                        Status.ACTIVE,
                        now
                );

        for (ScheduledTransaction schedule : dueList) {
            // startDate / endDate 범위 내인지 한 번 더 확인
            LocalDate today = now.toLocalDate();
            if (schedule.getStartDate() != null && today.isBefore(schedule.getStartDate())) {
                continue;
            }
            if (schedule.getEndDate() != null && today.isAfter(schedule.getEndDate())) {
                // 기간 넘었으면 COMPLETED 처리
                schedule.setStatus(Status.COMPLETED);
                schedule.setNextRunAt(null);
                schedule.setUpdatedAt(now);
                continue;
            }

            // TODO: 여기서 실제 이체(TransactionService 호출) + LogService 호출 등 처리

        }
    }

    // ================== 5. nextRunAt 계산 헬퍼 ==================

    private LocalDateTime recalculateNextRunAt(ScheduledTransaction schedule) {
        // lastRunAt가 있으면 그 기준으로, 없으면 startDate 기준으로 계산
        LocalDateTime base = schedule.getLastRunAt();
        if (base == null) {
            base = schedule.getStartDate().atTime(9, 0);
        }
        return calculateNextRunAt(schedule.getFrequency(), base);
    }

    private LocalDateTime calculateNextRunAt(Frequency frequency, LocalDateTime base) {
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
            default:
                // CUSTOM 등은 rrule 기반 계산이 필요할 수 있음
                return null;
        }
    }
}
