package com.db.bank.apiPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum Status {
    TEMP_SUCCESS("200", "SUCCESS", "임시 API 접근에 성공했습니다."),
    //계좌
    ACCOUNT_FORBIDDEN("403", "FAILURE", "접근 불가 계좌입니다."),
    ACCOUNT_ALREADY_PRESENT("409", "FAILURE", "이미 존재하는 계좌입니다."),
    ACCOUNT_NON_PRESENT("404", "FAILURE", "존재하지 않는 계좌입니다."),
    //예약 이체
    INVALID_SCHEDULED_TRANSACTION_AMOUNT("400", "FAILURE", "예약이체 금액은 0보다 커야 합니다."),
    INVALID_SCHEDULED_TRANSACTION_STARTDATE("400", "FAILURE", "예약이체 시작일은 필수입니다."),
    SCHEDULED_TRANSACTION_ALREADY_PRESENT("409", "FAILURE", "이미 존재하는 예약이체입니다."),
    SCHEDULED_TRANSACTION_NON_PRESENT("404", "FAILURE", "존재하지 않는 예약이체입니다."),
    SCHEDULED_TRANSACTION_FORBIDDEN("403", "FAILURE", "접근 불가 예약이체입니다."),
    INVALID_SCHEDULED_TRANSACTION_TIME("400", "FAILURE", "예약 실행 시간이 올바르지 않습니다."),
    SCHEDULED_TRANSACTION_ALREADY_FINISHED("409", "FAILURE", "이미 종료된 예약이체입니다."),
    INVALID_SCHEDULE_STATUS_FOR_PAUSE("409", "FAILURE", "ACTIVE 상태의 예약이체만 일시정지할 수 있습니다."),
    INVALID_SCHEDULE_STATUS_FOR_RESUME("409", "FAILURE", "PAUSED 상태의 예약이체만 재개할 수 있습니다."),
    SCHEDULED_TRANSACTION_NOT_FOUND("404", "FAILURE", "예약이체를 찾을 수 없습니다."),


    //로그
    INVALID_LOG_ARGUMENT("404", "FAILURE", "로그 기록을 위한 transaction/account/actorUser는 null일 수 없습니다."),
    //사용자
    USER_NON_PRESENT("404", "FAILURE", "존재하지 않는 사용자입니다.");
    private final String code;
    private final String result;
    private final String message;
}
