package com.db.bank.service;

import com.db.bank.apiPayload.exception.AccountException;
import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.TransferLimit;
import com.db.bank.domain.enums.transferLimit.TransferStatus;
import com.db.bank.repository.AccountRepository;
import com.db.bank.repository.TransferLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final로 자동 생성자
@Transactional //트랜잭션 내에서
public class TransferLimitService {
    private final TransferLimitRepository transferLimitRepository;
    private final AccountRepository accountRepository;

    // 특정계좌의 활성이체한도 조회
    @Transactional(readOnly = true)
    public Optional<TransferLimit> getActiveTransferLimit(String accountNum) {
        //계좌번호로 Account 엔티티 조회
        Account account =accountRepository.findByAccountNum(accountNum)
                .orElseThrow(()-> new AccountException.AccountNonExistsException("계좌없음:"+accountNum));
        return transferLimitRepository.findByAccountAndStatus(account, TransferStatus.ACTIVE);
    }

    // 이체한도를 계좌에 등록
    public TransferLimit createTransferLimit(String accountNum, BigDecimal dailyLimit, BigDecimal perTxLimit, String note) {
        // 계좌번호로 조회
        Account account=accountRepository.findByAccountNum(accountNum)
                .orElseThrow(()-> new AccountException.AccountNonExistsException("계좌없음:"+accountNum));
        //객체 생성
        TransferLimit limit = TransferLimit.builder()
                .account(account) //계좌
                .dailyLimitAmt(dailyLimit) //하루한도
                .perTxLimitAmt(perTxLimit) //1회한도
                .startDate(LocalDateTime.now()) //시작은현재로
                .note(note)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())//수정시각
                .build();
        return transferLimitRepository.save(limit);
    }

    // 과거 이체한도 기록들을 조회 (활성+비활성 모두) -> 관리자 or 마이페이지 사용가능
    @Transactional(readOnly = true) // 읽기 전용
    public List<TransferLimit> getPastTransferLimits(String accountNum) {
        // 계좌번호로 Account 조회
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌 없음: " + accountNum));

        // 계좌의 상태 이체한도 리스트 반환
        return transferLimitRepository.findAllByAccount(account);
    }

}
