package com.db.bank.repository;

import com.db.bank.domain.entity.Log;
import com.db.bank.domain.enums.log.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    // 1. 페이징으로 전체 조회 (계좌 번호)
    Page<Log> findByAccountAccountNumOrderByCreatedAtDesc(String accountNum, Pageable pageable);
    // 2. 페이징으로 전체 조회 (사용자)
    Page<Log> findByActorUserIdOrderByCreatedAtDesc(Long actorUserId , Pageable pageable);

    // 3. 계좌 + 기간 필터 + 페이징
    Page<Log> findByAccountAccountNumAndCreatedAtBetweenOrderByCreatedAtDesc(
            String accountNum,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // 4. 액션 타입별 (입금만, 출금만, 이상거래 등)
    Page<Log> findByActionOrderByCreatedAtDesc(
            Action action,
            Pageable pageable
    );





}
