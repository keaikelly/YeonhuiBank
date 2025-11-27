package com.db.bank.repository;

import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.TransferLimit;
import com.db.bank.domain.enums.transferLimit.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferLimitRepository extends JpaRepository<TransferLimit, Long> {

    // 1. 계좌번호와 이체한도 상태로 이체한도 1개 조회(active 조회할때 사용)
    Optional<TransferLimit> findOneByAccountAndStatus(Account account, TransferStatus status);

    // 2. 계좌번호와 이체한도의 상태로 이체한도조회
    List <TransferLimit> findByAccountAndStatus(Account account, TransferStatus status);

    // 3. 특정계좌의 모든 이체한도 조회
    List<TransferLimit> findAllByAccount(Account account);

    // 4. 특정계좌의 특정상태의 모든 이체한도 조회
    List<TransferLimit> findAllByAccountAndStatus(Account account, TransferStatus status);

}
