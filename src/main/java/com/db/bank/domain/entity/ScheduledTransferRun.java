package com.db.bank.domain.entity;

import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "scheduled_transfer_run")
public class ScheduledTransferRun {

    // 실행 로그 id (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_id")
    private Long id;

    // 예약참조 (FK → scheduled_transaction.schedule_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduledTransaction schedule;

    // 실행 시간(runTime) 저장
    @Column(name = "run_time", nullable = false)
    private LocalTime runTime;

    // 실행 시각
    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    // 결과 ENUM('success','insufficient_funds','error','skipped')
    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 30)
    private RunResult result;

    // 실패/감사 사유 메시지
    @Column(name = "message", length = 1000)
    private String message;

    // 출금 거래 (FK → transaction.transaction_id), NULL, unique 예약 이체 실패 시 트랜잭션 안만들어짐
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "txn_out_id",  unique = true)
    private Transaction txnOut;

    // 입금 거래 (FK → transaction.transaction_id), NULL 가능, unique
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "txn_in_id", unique = true)
    private Transaction txnIn;

    // 사유 코드 (실패 코드 등)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "failure_reason_code") // FK 컬럼명
    private TransferFailureReason failureReason;

    // 재시도 횟수 (기본값 0)
    @Column(name = "retry_no", nullable = false,
            columnDefinition = "INT DEFAULT 0")
    private int retryNo;

    // 허용된 재시도 횟수 (기본값 3)
    @Column(name = "max_retries", nullable = false,
            columnDefinition = "INT DEFAULT 3")
    private int maxRetries;

    // 다음 재시도 수행 시각
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
}
