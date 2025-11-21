package com.db.bank.apiPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum Status {
    TEMP_SUCCESS("200", "SUCCESS", "임시 API 접근에 성공했습니다."),

    ACCOUNT_NON_PRESENT("404", "FAILURE", "존재하지 않는 버킷리스트입니다.");
    private final String code;
    private final String result;
    private final String message;
}
