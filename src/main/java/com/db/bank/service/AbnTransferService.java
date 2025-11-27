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

    //계좌번호로 이상거래 가져오기
    @Transactional(readOnly = true)
    public List<AbnTransfer> getAllAbnTransfersByAccount(String accountNum) {
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌를 찾을 수 없습니다."));
        return abnTransferRepository.findAllByAccountNum(account);
    }

    @Transactional
    public void detectAbnTransfer(Transaction t){
        String from=t.getFromAccount().getAccountNum();
        String to=t.getToAccount().getAccountNum();
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime AgoMinute =now.minusMinutes(10); //10분이전으로 지정

        //최근 10분내 동일거래 3회 이상 감지
        Long count=transactionRepository.sameTransfer(from, to, t.getAmount(), AgoMinute);
        if(count>=3) createAbnTransfer(t.getId(), from, RuleCode.MULTI_TRANSFER_SAME_ACCOUNT, "10분내 동일거래 3건 이상");

        //일일이체 한도초과
        LocalDateTime todayStart=now.toLocalDate().atStartOfDay();
        BigDecimal totalToday=transactionRepository.getTotalTransferredToday(from, todayStart, now);
        Account fromAccount =t.getFromAccount();

        Optional<TransferLimit> limitOpt =transferLimitRepository.findOneByAccountAndStatus(fromAccount, TransferStatus.ACTIVE);

        if(limitOpt.isPresent()) {
            BigDecimal dailyLimit = limitOpt.get().getDailyLimitAmt();
            if (totalToday.compareTo(dailyLimit) > 0) {
                createAbnTransfer(t.getId(), from, RuleCode.DAILY_TOTAL_EXCEEDED, "일일 이체 한도 초과");
            }
        }

        //수취인 첫 계좌 알림
        Long history= transactionRepository.countHistoryBetweenAccounts(from, to);
        if(history==0) createAbnTransfer(t.getId(), from, RuleCode.NEW_RECEIVER, "처음 보내는 수취인 계좌");

    }

}
