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
    INVALID_ACCOUNT_AMOUNT("400", "FAILURE", "계좌 거래 금액이 유효하지 않습니다."),
    INVALID_ACCOUNT_ARGUMENT("400", "FAILURE", "계좌 요청 파라미터가 유효하지 않습니다."),
    INSUFFICIENT_BALANCE("409", "FAILURE", "계좌 잔액이 부족합니다."),
    ACCOUNT_CREATE_SUCCESS("201","SUCCESS","계좌 생성을 완료했습니다."),
    ACCOUNT_READ_SUCCESS("200","SUCCESS","해당 계좌를 불러왔습니다"),
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
    SCHEDULE_CREATE_SUCCESS("201","SUCCESS","예약 이체 생성을 성공하였습니다."),
    SCHEDULE_READ_SUCCESS("200","SUCCESS","예약 이체 조회를 성공하였습니다."),
    SCHEDULE_UPDATE_SUCCESS("200", "SUCCESS","예약 이체 수정을 성공하였습니다."),
    SCHEDULE_CANCEL_SUCCESS("204","SUCCESS","예약 이체 취소를 성공하였습니다."),
    SCHEDULE_RUN_READ_SUCCESS("200", "SUCCESS","예약이체 실행 이력 조회 성공"),

    //로그
    INVALID_LOG_ARGUMENT("404", "FAILURE", "로그 기록을 위한 transaction/account/actorUser는 null일 수 없습니다."),
    LOG_READ_SUCCESS("200", "SUCCESS","로그 조회에 성공했습니다."),
    //사용자
    USER_NON_PRESENT("404", "FAILURE", "존재하지 않는 사용자입니다."),
    USER_INVALID_LOGIN("401","FAILURE","아이디 또는 비밀번호가 일치하지 않습니다"),
    USER_SIGNUP_SUCCESS("201", "SUCCESS", "회원가입을 완료했습니다."),
    USER_LOGIN_SUCCESS("200", "SUCCESS", "로그인을 완료했습니다."),
    USER_CHECK_ID("200", "SUCCESS", "사용 가능한 아이디입니다."),
    USER_READ_SUCCESS("200", "SUCCESS", "유저 정보를 성공적으로 조회했습니다."),

    //트랜잭션
    TRANSACTION_NON_PRESENT ("404", "FAILURE", "존재하지 않는 트랜잭션 입니다."),
    TRANSACTION_CREATE_SUCCESS("201","SUCCESS","거래 생성을 성공하였습니다."),
    TRANSACTION_READ_SUCCESS("200", "SUCCESS","거래 조회를 성공하였습니다."),

    //이체 한도
    TRANSFER_LIMIT_CREATE_SUCCESS("201", "SUCCESS","이체 한도 생성/수정 성공"),
    TRANSFER_LIMIT_READ_SUCCESS("200", "SUCCESS","활성 이체 한도 조회 성공"),
    TRANSFER_LIMIT_HISTORY_READ_SUCCESS("200", "SUCCESS", "이체 한도 이력 조회 성공"),
    TRANSFER_LIMIT_ENDDATE_UPDATE_SUCCESS("200", "SUCCESS","이체 한도 종료일 수정 성공"),

    //이상거래
    ABN_TRANSFER_READ_SUCCESS("200","SUCCESS","이상거래 조회 성공");

    private final String code;
    private final String result;
    private final String message;
}
