package com.db.bank.repository;

import com.db.bank.domain.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>  {

    // 1. 계좌번호로 조회
    Optional<Account> findByAccountNum(String accountNum);

    // 2. 계좌번호 중복 여부 체크 (계좌 생성 시 사용)
    boolean existsByAccountNum(String accountNum);

    // 3. 특정 유저의 전체 계좌 목록
    List<Account> findAllByUserId(Long userId);

    // 4. 생성일 기준으로 정렬(마이페이지)
    Page<Account> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 5.유저 소유 계좌인지 검증
    Optional<Account> findByIdAndUserId(Long id, Long userId);

    // 6. 유저별 총 자산 합계 (대시보드용)
    @Query("select coalesce(sum(a.balance), 0) from Account a where a.user.id = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") Long userId);

    // 7. 계좌 조회 시 DB가 그 Row 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNum = :accountNum")
    Optional<Account> findByAccountNumForUpdate(String accountNum);

    Optional<Account> findExternalInAccount();
    Optional<Account> findExternalOutAccount();
}
