package com.db.bank.repository;

import com.db.bank.domain.entity.ScheduledTransaction;
import com.db.bank.domain.enums.scheduledTransaction.ScheduledStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

    // 1. 특정 유저가 만든 예약이체 목록
    Page<ScheduledTransaction> findByCreatedByIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    // 2. 특정 계좌 기준 (출금 계좌가 본 계좌)
    Page<ScheduledTransaction> findByFromAccountIdOrderByCreatedAtDesc(
            Long fromAccountId,
            Pageable pageable
    );

    // 3. 유저 + 상태 (활성/취소 필터)
    Page<ScheduledTransaction> findByCreatedByIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            ScheduledStatus scheduledStatus,
            Pageable pageable
    );

    // ================== 실행용 쿼리 (스케줄러/배치) ==================

    // 4. 지금 실행해야 할 예약이체들 가져오기
    List<ScheduledTransaction> findByStatusAndNextRunAtLessThanEqual(
            ScheduledStatus scheduledStatus,
            LocalDateTime now
    );

    // 5. 날짜 기반 (start_date ~ end_date 안에 있는 ACTIVE 예약들)
    List<ScheduledTransaction> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            ScheduledStatus scheduledStatus,
            LocalDate startDate,
            LocalDate endDate
    );

    // 6. 현재 실행해야할 예약이체 모음
    List<ScheduledTransaction> findTop100ByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(
            ScheduledStatus scheduledStatus,
            LocalDateTime now
    );

    // 7. 중복 체크
    boolean existsByFromAccountIdAndToAccountIdAndStatus(
            Long fromAccountId,
            Long toAccountId,
            ScheduledStatus scheduledStatus
    );

}
