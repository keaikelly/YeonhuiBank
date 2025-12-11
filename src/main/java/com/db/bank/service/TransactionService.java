package com.db.bank.service;


import com.db.bank.apiPayload.exception.AccountException;
import com.db.bank.apiPayload.exception.UserException;
import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.Transaction;
import com.db.bank.domain.entity.User;
import com.db.bank.domain.enums.transaction.TransactionStatus;
import com.db.bank.domain.enums.transaction.TransactionType;
import com.db.bank.repository.AccountRepository;
import com.db.bank.repository.TransactionRepository;
import com.db.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.db.bank.domain.enums.account.AccountType.EXTERNAL_IN;
import static com.db.bank.domain.enums.account.AccountType.EXTERNAL_OUT;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final LogService logService;
    private final AbnTransferService abnTransferService;
    private Account getExternalInAccount() {
        return accountRepository.findByAccountType(EXTERNAL_IN)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("EXTERNAL_IN 계좌가 없습니다."));
    }

    private Account getExternalOutAccount() {
        return accountRepository.findByAccountType(EXTERNAL_OUT)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("EXTERNAL_OUT 계좌가 없습니다."));
    }
    // ================== 1. 공통 유효성 체크 ==================

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException.InvalidAccountAmountException("거래 금액은 0보다 커야 합니다.");
        }
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNonExistsException("사용자를 찾을 수 없습니다. id=" + userId));
    }

    // ================== 2. 입금 ==================


    //입금: fromAccount: null / toAccount
    @Transactional
    public Transaction deposit(
            Long userId,
            String toAccountNum,
            BigDecimal amount,
            String memo
    ) {
        validateAmount(amount);
        User actor = loadUser(userId);

        // 잠금 걸고 계좌 조회
        Account toAccount = accountRepository.findByAccountNumForUpdate(toAccountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("입금 계좌를 찾을 수 없습니다. accountNum=" + toAccountNum));

        Account externalInAccount = getExternalInAccount();

        BigDecimal before = toAccount.getBalance();
        BigDecimal after = before.add(amount);
        toAccount.setBalance(after);

        Transaction tx = Transaction.builder()
                .fromAccount(externalInAccount)
                .toAccount(toAccount)
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .memo(memo)
                .build();

        tx = transactionRepository.save(tx);

        // 로그 기록
        logService.logDeposit(tx, toAccount, before, after, actor);

        return tx;
    }

    // ================== 3. 출금 ==================

    /**
     * 출금
     * - fromAccountNum 계좌에서 amount 만큼 출금
     * - 출금은 반드시 자신의 계좌에서만 가능 (소유자 검증)
     */
    @Transactional
    public Transaction withdraw(
            Long userId,
            String fromAccountNum,
            BigDecimal amount,
            String memo
    ) {
        validateAmount(amount);
        User actor = loadUser(userId);

        Account fromAccount = accountRepository.findByAccountNumForUpdate(fromAccountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("출금 계좌를 찾을 수 없습니다. accountNum=" + fromAccountNum));
        Account externalOutAccount = getExternalOutAccount();

        // 소유자 검증
        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 출금 계좌는 이 사용자의 계좌가 아닙니다.");
        }

        BigDecimal before = fromAccount.getBalance();
        if (before.compareTo(amount) < 0) {
            throw new AccountException.InsufficientBalanceException("잔액이 부족합니다.");
        }

        BigDecimal after = before.subtract(amount);
        fromAccount.setBalance(after);

        Transaction tx = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(externalOutAccount)
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .memo(memo)
                .build();

        tx = transactionRepository.save(tx);

        // 로그 기록
        logService.logWithdraw(tx, fromAccount, before, after, actor);
        abnTransferService.postCheckAbnTransfer(tx);
        return tx;
    }

    // ================== 4. 이체 ==================

    /**
     * 계좌 이체
     * - fromAccountNum -> toAccountNum 으로 amount 만큼 보냄
     * - 출금 계좌는 반드시 자신의 계좌여야 함 (소유자 검증)
     */
    @Transactional
    public Transaction transfer(
            Long userId,
            String fromAccountNum,
            String toAccountNum,
            BigDecimal amount,
            String memo
    ) {
        validateAmount(amount);
        if (fromAccountNum.equals(toAccountNum)) {
            throw new AccountException.InvalidAccountArgumentException("출금 계좌와 입금 계좌가 같을 수 없습니다.");
        }

        User actor = loadUser(userId);

        // 잠금 걸고 두 계좌 조회
        Account fromAccount = accountRepository.findByAccountNumForUpdate(fromAccountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("출금 계좌를 찾을 수 없습니다. accountNum=" + fromAccountNum));

        Account toAccount = accountRepository.findByAccountNumForUpdate(toAccountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("입금 계좌를 찾을 수 없습니다. accountNum=" + toAccountNum));

        // 소유자 검증 (fromAccount 만)
        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 출금 계좌는 이 사용자의 계좌가 아닙니다.");
        }

        BigDecimal fromBefore = fromAccount.getBalance();
        if (fromBefore.compareTo(amount) < 0) {
            throw new AccountException.InsufficientBalanceException("잔액이 부족합니다.");
        }
        abnTransferService.preCheckAbnTransfer(fromAccount, toAccount, amount);

        BigDecimal toBefore = toAccount.getBalance();

        BigDecimal fromAfter = fromBefore.subtract(amount);
        BigDecimal toAfter = toBefore.add(amount);

        fromAccount.setBalance(fromAfter);
        toAccount.setBalance(toAfter);

        Transaction tx = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .type(TransactionType.TRANSFER)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .memo(memo)
                .build();

        tx = transactionRepository.save(tx);
        abnTransferService.postCheckAbnTransfer(tx);
        // 로그 기록 (보낸 쪽 / 받은 쪽 따로)
        logService.logTransferDebit(tx, fromAccount, fromBefore, fromAfter, actor);
        logService.logTransferCredit(tx, toAccount, toBefore, toAfter, actor);

        return tx;
    }

    // ================== 5. 조회용 메서드 ==================

    // 5-1. 내가 보낸 거래 내역 (fromAccount 기준)
    @Transactional(readOnly = true)
    public Page<Transaction> getSentTransactions(
            Long userId,
            Long fromAccountId,
            Pageable pageable
    ) {
        // 출금 계좌 소유자 검증
        Account account = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌를 찾을 수 없습니다. id=" + fromAccountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 계좌에 대한 조회 권한이 없습니다.");
        }

        return transactionRepository.findByFromAccountIdOrderByCreatedAtDesc(fromAccountId, pageable);
    }

    // 5-2. 내가 받은 거래 내역 (toAccount 기준)
    @Transactional(readOnly = true)
    public Page<Transaction> getReceivedTransactions(
            Long userId,
            Long toAccountId,
            Pageable pageable
    ) {
        Account account = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌를 찾을 수 없습니다. id=" + toAccountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 계좌에 대한 조회 권한이 없습니다.");
        }

        return transactionRepository.findByToAccountIdOrderByCreatedAtDesc(toAccountId, pageable);
    }
}
