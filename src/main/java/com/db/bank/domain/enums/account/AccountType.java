package com.db.bank.domain.enums.account;

public enum AccountType {
    NORMAL,          // 일반 사용자 계좌
    EXTERNAL_IN,     // 외부 → 내부로 들어오는 가상 계좌
    EXTERNAL_OUT     // 내부 → 외부로 나가는 가상 계좌
}