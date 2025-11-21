package com.db.bank.domain.enums.log;

public enum Action {
    DEPOSIT,        // 입금
    WITHDRAW,       // 출금
    FRAUD,          // 불법/이상거래
    ADJUST,          // 수정
    TRANSFER_DEBIT, // 이체 보낸 쪽
    TRANSFER_CREDIT // 이체 받은 쪽



}
