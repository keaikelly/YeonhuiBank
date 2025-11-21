package com.db.bank.repository;

import com.db.bank.domain.entity.Transaction;
import com.db.bank.domain.enums.transaction.TransactionStatus;
import com.db.bank.domain.enums.transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 1. fromAccount 기준 조회
    Page<Transaction> findByFromAccountIdOrderByCreatedAtDesc(
            Long fromAccountId,
            Pageable pageable
    );

    // 2. toAccount 기준 조회
    Page<Transaction> findByToAccountIdOrderByCreatedAtDesc(
            Long toAccountId,
            Pageable pageable
    );

    // ==== 3. 거래 타입별 조회 =====

    // 1) 입금 ToAccount = myAccount, 이체(받음) 조회;
    Page<Transaction> findByToAccountAccountNumAndTypeOrderByCreatedAtDesc(
            String toAccountNum,
            TransactionType type,
            Pageable pageable
    );

    // 2) 출금 fromAccount = myAccount, 이체(보냄) 조회
    Page<Transaction> findByFromAccountAccountNumAndTypeOrderByCreatedAtDesc(
            String fromAccountNum,
            TransactionType type,
            Pageable pageable
    );

    // 3) 거래 타입 + 기간(Date Range)
    Page<Transaction> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // 4) 거래 타입 + 거래 상태(Status)
    Page<Transaction> findByTypeAndStatusOrderByCreatedAtAsc(
            TransactionType type,
            TransactionStatus status,
            Pageable pageable
    );


    // 5) 거래 타입 + 금액 범위
    Page<Transaction> findByTypeAndAmountBetweenOrderByAmountAsc(
            TransactionType type,
            BigDecimal min,
            BigDecimal max,
            Pageable pageable

    );
    // ==== 4. 거래 상태별 조회 ====

    // 1) 상태별 거래 목록 조회
    Page<Transaction> findByStatusOrderByCreatedAtDesc(
            TransactionStatus status,
            Pageable pageable
    );

    // 2) 계좌 + 상태
    Page<Transaction> findByFromAccountIdAndStatusOrderByCreatedAtDesc(
            Long fromAccountId,
            TransactionStatus status,
            Pageable pageable
    );

    // 3) 상태 + 기간
    Page<Transaction> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            TransactionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // 4) 긴 시간 동안 PENDING인 거래 찾기
    Page<Transaction> findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
            TransactionStatus status,
            LocalDateTime threshold,
            Pageable pageable
    );


    // ==== 5. 기간별 조회 ====
    // 1) 기간별 전체 거래 조회
    Page<Transaction> findByCreatedAtBetweenOOrderByCreatedAtCreatedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
    // 2) 계좌 + 기간
    Page<Transaction> findByFromAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long fromAccountId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
    // 3) 계좌 + 기간
    Page<Transaction> findByToAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long toAccountId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );


}
