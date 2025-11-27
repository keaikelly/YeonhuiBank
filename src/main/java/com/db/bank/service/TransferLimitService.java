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

    // 특정계좌의 활성이체한도 조회 (하루, 건당이 리스트로)
    @Transactional(readOnly = true)
    public List<TransferLimit> getActiveTransferLimit(Long userId, String accountNum) {
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌없음:" + accountNum));

        // 소유자 검증
        if (!account.getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 계좌는 이 사용자의 계좌가 아닙니다.");
        }

        return transferLimitRepository.findByAccountAndStatus(account, TransferStatus.ACTIVE);
    }

    // 이체한도를 계좌에 등록
    // 2) 생성/변경
    @Transactional
    public TransferLimit createTransferLimit(
            Long userId,
            String accountNum,
            BigDecimal dailyLimit,
            BigDecimal perTxLimit,
            String note
    ) {
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌없음:" + accountNum));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 계좌는 이 사용자의 계좌가 아닙니다.");
        }
        // 기존 ACTIVE 상태의 이체한도가 있다면 비활성화 처리
        List<TransferLimit> existingLimits = transferLimitRepository.findByAccountAndStatus(account, TransferStatus.ACTIVE);
        for (TransferLimit limit : existingLimits) {
            limit.setStatus(TransferStatus.INACTIVE);
            limit.setUpdatedAt(LocalDateTime.now());
        }

        //객체 생성
        TransferLimit limit = TransferLimit.builder()
                .account(account) //계좌
                .dailyLimitAmt(dailyLimit) //하루한도
                .perTxLimitAmt(perTxLimit) //1회한도
                .startDate(LocalDateTime.now()) //시작은현재로
                .status(TransferStatus.ACTIVE) //active 명시
                .note(note)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())//수정시각
                .build();
        return transferLimitRepository.save(limit);
    }

    // 과거 이체한도 기록들을 조회 (활성+비활성 모두) -> 관리자 or 마이페이지 사용가능
    @Transactional(readOnly = true)
    public List<TransferLimit> getPastTransferLimits(Long userId, String accountNum) {
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌 없음: " + accountNum));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 계좌는 이 사용자의 계좌가 아닙니다.");
        }

        return transferLimitRepository.findAllByAccount(account);
    }
    @Transactional
    public TransferLimit updateEndDate(Long userId, Long limitId, LocalDateTime endDate) {
        TransferLimit limit = transferLimitRepository.findById(limitId)
                .orElseThrow(() -> new IllegalArgumentException("이체한도 정보를 찾을 수 없습니다. id=" + limitId));

        // 소유자 검증
        if (!limit.getAccount().getUser().getId().equals(userId)) {
            throw new AccountException.UnauthorizedAccountAccessException("해당 이체한도에 대한 수정 권한이 없습니다.");
        }

        limit.setEndDate(endDate);
        limit.setUpdatedAt(LocalDateTime.now());
        return limit;
    }


}
