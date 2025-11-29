package com.db.bank.repository;

import com.db.bank.domain.entity.ScheduledTransferRun;
import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledTransferRunRepository extends JpaRepository<ScheduledTransferRun, Long> {

    // 특정 예약이체의 실행 로그 전체 조회 (최신순)
    List<ScheduledTransferRun> findByScheduleIdOrderByExecutedAtDesc(Long scheduleId);

    // 스케줄 + 결과 기준 조회
    List<ScheduledTransferRun> findByScheduleIdAndResultOrderByExecutedAtDesc(Long scheduleId, RunResult result);

    // 재시도 필요한 실행 로그 조회 (retry_no < max_retries AND next_retry_at <= now)
    List<ScheduledTransferRun> findByRetryNoLessThanAndNextRetryAtLessThanEqual(
            int maxRetryCheck,
            LocalDateTime now
    );
    // 특정 예약(schedule)의 실행 로그 중 실패한 것만 조회
    List<ScheduledTransferRun> findByScheduleIdAndResultNotOrderByExecutedAtDesc(
            Long scheduleId,
            RunResult result
    );
    // 실패한 실행 로그 조회
    List<ScheduledTransferRun> findByResult(RunResult result);

}
