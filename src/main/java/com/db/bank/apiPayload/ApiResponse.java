package com.db.bank.apiPayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
@JsonPropertyOrder({"code", "result", "message", "data"})
public class ApiResponse<T>{
    private final String code;
    private final String result;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // 성공한 경우 응답 생성
    public static <T> ApiResponse<T> onSuccess(Status status, T data){
        return new ApiResponse<>(status.getCode(), status.getResult(), status.getMessage(), data);
    }

    // 실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(Status status){
        return new ApiResponse<>(status.getCode(), status.getResult(), status.getMessage(), null);
    }

}
