package com.db.bank.service;

import com.db.bank.apiPayload.exception.ScheduledTransactionException;
import com.db.bank.domain.entity.ScheduledTransaction;
import com.db.bank.domain.entity.ScheduledTransferRun;
import com.db.bank.domain.entity.Transaction;
import com.db.bank.domain.entity.TransferFailureReason;
import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import com.db.bank.repository.ScheduledTransferRunRepository;
import com.db.bank.repository.ScheduledTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTransferRunService {

    private final ScheduledTransferRunRepository runRepository;
    private final ScheduledTransactionRepository scheduledTransactionRepository;


    // 1) 실행 로그 기록 (공통)
    @Transactional//(propagation = Propagation.REQUIRES_NEW)
    public ScheduledTransferRun recordRun(
            ScheduledTransaction schedule,
            Transaction txnOut,
            Transaction txnIn,
            RunResult result,
            String message,
            TransferFailureReason failureReasonCode,
            int retryNo,
            int maxRetries,
            LocalDateTime nextRetryAt,
            LocalDateTime executedAt
    ) {
        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
        LocalTime runTime = null;
        if (schedule.getRunTime() != null) {
            runTime = schedule.getRunTime();
        } else if (schedule.getNextRunAt() != null) {
            runTime = schedule.getNextRunAt().toLocalTime();
        } else {
            runTime = executedAt.toLocalTime();
        }

        ScheduledTransferRun run = ScheduledTransferRun.builder()
                .schedule(schedule)
                .txnOut(txnOut)
                .runTime(runTime)
                .txnIn(txnIn)
                .result(result)
                .message(message)
                .failureReason(failureReasonCode)
                .retryNo(retryNo)
                .maxRetries(maxRetries)
                .nextRetryAt(nextRetryAt)
                .executedAt(executedAt)
                .build();

        return runRepository.save(run);
    }

    // 성공 케이스 편의 메서드 (retry 0, failureReasonCode 없음)
    @Transactional //(propagation = Propagation.REQUIRES_NEW)
    public ScheduledTransferRun recordSuccess(
            ScheduledTransaction schedule,
            Transaction txnOut,
            Transaction txnIn,
            String message
    ) {
        return recordRun(
                schedule,
                txnOut,
                txnIn,
                RunResult.SUCCESS,
                message,
                null,
                0,
                schedule != null ? 0 : 0,  // 필요하면 maxRetries 정책에 맞게 수정
                null,
                LocalDateTime.now()
        );
    }

    // 실패/재시도 케이스 편의 메서드
    @Transactional//(propagation = Propagation.REQUIRES_NEW)
    public ScheduledTransferRun recordFailure(
            ScheduledTransaction schedule,
            Transaction txnOut,
            Transaction txnIn,
            RunResult result,
            String message,
            TransferFailureReason failureReason,
            int retryNo,
            int maxRetries,
            LocalDateTime nextRetryAt
    ) {
        return recordRun(
                schedule,
                txnOut,
                txnIn,
                result,
                message,
                failureReason,
                retryNo,
                maxRetries,
                nextRetryAt,
                LocalDateTime.now()
        );
    }


    // 2) 조회용 메서드 (Page 사용 – 화면/관리자용)

    // 특정 예약이체의 실행 로그 (최신순, 페이징)
    @Transactional(readOnly = true)
    public List<ScheduledTransferRun> getRunsBySchedule(
            Long scheduleId,
            Pageable pageable
    ) {
        // 스케줄이 실제 존재하는지 체크(옵션)
        scheduledTransactionRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이체를 찾을 수 없습니다. id=" + scheduleId));

        return runRepository.findByScheduleIdOrderByExecutedAtDesc(scheduleId);
    }

    // 특정 예약이체 + 결과 기준 실행 로그 (성공만, 실패만 등)
    @Transactional(readOnly = true)
    public List<ScheduledTransferRun> getRunsByScheduleAndResult(
            Long scheduleId,
            RunResult result,
            Pageable pageable
    ) {
        scheduledTransactionRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이체를 찾을 수 없습니다. id=" + scheduleId));

        return runRepository.findByScheduleIdAndResultOrderByExecutedAtDesc(scheduleId, result);
    }


    // 3) 재시도 대상 실행 로그 조회 (배치/내부용 – List 사용)

    /**
     * 재시도 대상 실행 로그 조회
     * - retry_no < maxRetryCheck
     * - next_retry_at <= now
     */
    @Transactional(readOnly = true)
    public List<ScheduledTransferRun> getRetryTargets(LocalDateTime now, int maxRetryCheck) {
        return runRepository.findByRetryNoLessThanAndNextRetryAtLessThanEqual(maxRetryCheck, now);
    }
    @Transactional(readOnly = true)
    public List<ScheduledTransferRun> getMyFailures(Long userId, Long scheduleId) {

        // 1️⃣ 예약이 존재하는지 체크
        ScheduledTransaction schedule = scheduledTransactionRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new ScheduledTransactionException.ScheduledTransactionNotFoundException(
                                "예약이체를 찾을 수 없습니다. id=" + scheduleId
                        )
                );

        // 2️⃣ 이 예약이 진짜 이 유저의 것인지 검증 (권한 체크)
        if (!schedule.getCreatedBy().getId().equals(userId)) {
            throw new ScheduledTransactionException.UnauthorizedScheduledTransaction(
                    "해당 예약이체에 접근 권한이 없습니다."
            );
        }

        // 3️⃣ 이 예약에 대한 실패 실행 로그만 조회
        return runRepository.findByScheduleIdAndResultNotOrderByExecutedAtDesc(
                schedule.getId(),
                RunResult.SUCCESS
        );
    }

}
