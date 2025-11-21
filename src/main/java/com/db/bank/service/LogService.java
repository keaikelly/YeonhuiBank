package com.db.bank.service;

import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.Log;
import com.db.bank.domain.entity.Transaction;
import com.db.bank.domain.entity.User;
import com.db.bank.domain.enums.log.Action;
import com.db.bank.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    // ================== 1. 공통 로그 기록 메서드 ==================

    @Transactional
    public void recordLog(
            Transaction transaction,
            Account account,
            BigDecimal beforeBalance,
            BigDecimal afterBalance,
            Action action,
            User actorUser
    ) {
        Log log = Log.builder()
                .transaction(transaction)
                .account(account)
                .actorUser(actorUser)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .action(action)
                .createdAt(LocalDateTime.now())
                .build();

        logRepository.save(log);

    }



    //입금 로그 기록
    @Transactional
    public void logDeposit(
            Transaction transaction,
            Account account,
            BigDecimal beforeBalance,
            BigDecimal afterBalance,
            User actorUser
    ) {
        recordLog(transaction,account,beforeBalance, afterBalance, Action.DEPOSIT,actorUser);
    }

    @Transactional
    public void logWithdraw(
            Transaction transaction,
            Account account,
            BigDecimal beforeBalance,
            BigDecimal afterBalance,
            User actorUser
    ) {
        recordLog(transaction,account,beforeBalance, afterBalance, Action.WITHDRAW ,actorUser);
    }

    @Transactional
    public void logTransferDebit( // 이체 보낸 쪽
                                  Transaction transaction,
                                  Account fromAccount,
                                  BigDecimal beforeBalance,
                                  BigDecimal afterBalance,
                                  User actorUser
    ) {
        recordLog(transaction,fromAccount,beforeBalance, afterBalance, Action.TRANSFER_DEBIT ,actorUser);
    }

    @Transactional
    public void logTransferCredit( // 이체 받은 쪽
                                   Transaction transaction,
                                   Account toAccount,
                                   BigDecimal beforeBalance,
                                   BigDecimal afterBalance,
                                   User actorUser
    ) {
        recordLog(transaction,toAccount,beforeBalance, afterBalance, Action.TRANSFER_CREDIT ,actorUser);
    }
    //이상거래(FRAUD) 로그 기록
    @Transactional
    public void logFraud(
            Transaction transaction,
            Account account,
            BigDecimal beforeBalance,
            BigDecimal afterBalance,
            User actorUser
    ) {
        recordLog(transaction, account, beforeBalance, afterBalance, Action.FRAUD, actorUser);
    }


    //관리자 또는 시스템 조정 로그 (잔액 강제 수정 등)
    @Transactional
    public void logAdjust(
            Transaction transaction,
            Account account,
            BigDecimal beforeBalance,
            BigDecimal afterBalance,
            User actorUser
    ) {
        recordLog(transaction, account, beforeBalance, afterBalance, Action.ADJUST, actorUser);
    }
    // ================== 2. 조회용 메서드들 ==================

    // 2-1. 계좌번호 기준 로그 조회 (마이페이지/계좌 상세)
    @Transactional(readOnly = true)
    public Page<Log> getLogsByAccount(
            String accountNum,
            Pageable pageable
    ) {
        return logRepository.findByAccountAccountNumOrderByCreatedAtDesc(accountNum, pageable);
    }

    // 2-2. 사용자 기준 로그 조회 (내가 한 모든 작업)
    @Transactional(readOnly = true)
    public Page<Log> getLogsByActorUser(
            Long actorUserId,
            Pageable pageable
    ) {
        return logRepository.findByActorUserIdOrderByCreatedAtDesc(actorUserId,pageable);
    }

    // 2-3. 계좌 + 기간
    @Transactional(readOnly = true)
    public Page<Log> getLogsByAccountAndPeriod(
            String accountNum,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        return logRepository.findByAccountAccountNumAndCreatedAtBetweenOrderByCreatedAtDesc(accountNum, start, end, pageable);
    }

    // 2-4. 액션 타입별 (입금만, 출금만, 이상거래만 등)
    @Transactional(readOnly = true)
    public Page<Log> getLogsByAction(
            Action action,
            Pageable pageable
    ) {
        return logRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }
}
