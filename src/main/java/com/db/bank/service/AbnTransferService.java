package com.db.bank.service;

import com.db.bank.apiPayload.exception.AccountException;
import com.db.bank.apiPayload.exception.TransactionException;
import com.db.bank.domain.entity.AbnTransfer;
import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.Transaction;
import com.db.bank.domain.entity.TransferLimit;
import com.db.bank.domain.enums.abnTransfer.RuleCode;
import com.db.bank.domain.enums.transferLimit.TransferStatus;
import com.db.bank.repository.AbnTransferRepository;
import com.db.bank.repository.AccountRepository;
import com.db.bank.repository.TransactionRepository;
import com.db.bank.repository.TransferLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AbnTransferService {

    private final AbnTransferRepository abnTransferRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransferLimitRepository transferLimitRepository;

    //이상거래등록
    @Transactional
    public AbnTransfer createAbnTransfer(Long transactionId, String accountNum, RuleCode ruleCode, String detailMessage) {
        Transaction transaction = transactionRepository.findById(transactionId).
                orElseThrow(()->new TransactionException.TransactionNonExistsException("존재하지 않는 트랜잭션"));
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(()->new AccountException.AccountNonExistsException("존재하지 않는 계좌입니다"));

        AbnTransfer abnTransfer=AbnTransfer.builder()
                .transactionId(transaction)
                .accountNum(account)
                .ruleCode(ruleCode)
                .detailMessage(detailMessage)
                .createdAt(LocalDateTime.now())
                .build();

        return abnTransferRepository.save(abnTransfer);

    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AbnTransfer createAbnTransferWithoutTx(
            String accountNum,
            RuleCode ruleCode,
            String detailMessage
    ) {
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("존재하지 않는 계좌입니다"));

        AbnTransfer abnTransfer = AbnTransfer.builder()
                .transactionId(null) // 여기서만 NULL 허용
                .accountNum(account)
                .ruleCode(ruleCode)
                .detailMessage(detailMessage)
                .createdAt(LocalDateTime.now())
                .build();

        return abnTransferRepository.save(abnTransfer);
    }

    @Transactional
    public void preCheckAbnTransfer(Account from, Account to, BigDecimal amount) {

        LocalDateTime now = LocalDateTime.now();
        String fromAcc = from.getAccountNum();
        String toAcc   = to.getAccountNum();



        // 일일 한도 초과 → 알림 + 예외(트랜잭션 생성 자체를 차단)
        BigDecimal today = Optional.ofNullable(
                transactionRepository.getTotalTransferredToday(
                        fromAcc,
                        now.toLocalDate().atStartOfDay(),
                        now
                )
        ).orElse(BigDecimal.ZERO);

        Optional<TransferLimit> limitOpt =
                transferLimitRepository.findOneByAccountAndStatus(from, TransferStatus.ACTIVE);

        if (limitOpt.isPresent()
                && today.add(amount).compareTo(limitOpt.get().getDailyLimitAmt()) > 0) {

            // 알림 로그 (트랜잭션 없이)
            createAbnTransferWithoutTx(
                    fromAcc,
                    RuleCode.DAILY_TOTAL_EXCEEDED,
                    "일일 이체 한도 초과"
            );

            // 실제 비즈니스 로직 차단
            throw new TransactionException.IllegalTransferException("일일 이체 한도 초과");
        }
    }
    @Transactional
    public void postCheckAbnTransfer(Transaction tx) {

        String fromAcc = tx.getFromAccount().getAccountNum();
        String toAcc   = tx.getToAccount().getAccountNum();
        BigDecimal amount = tx.getAmount();
        LocalDateTime now = tx.getCreatedAt() != null
                ? tx.getCreatedAt()
                : LocalDateTime.now();

        LocalDateTime ago10 = now.minusMinutes(10);

        // 1) 최근 10분 내 동일 거래 3건 이상 → 트랜잭션 포함해서 로그 남김
        Long count = transactionRepository.sameTransfer(
                fromAcc,
                toAcc,
                amount,
                ago10
        );
        if (count >= 3) {
            createAbnTransfer(
                    tx.getId(),
                    fromAcc,
                    RuleCode.MULTI_TRANSFER_SAME_ACCOUNT,
                    "10분내 동일거래 3건 이상"
            );
        }

        // 2) 처음 보내는 수취인 → 트랜잭션 포함해서 로그 남김
        Long history = transactionRepository.countHistoryBetweenAccounts(fromAcc, toAcc);
        if (history == 1) {
            createAbnTransfer(
                    tx.getId(),
                    fromAcc,
                    RuleCode.NEW_RECEIVER,
                    "처음 보내는 수취인 계좌"
            );
        }
    }
    //조회
    @Transactional(readOnly = true)
    public List<AbnTransfer> getAllAbnTransfersByAccount(String accountNum) {
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌를 찾을 수 없습니다."));
        return abnTransferRepository.findAllByAccountNum(account);
    }

}
