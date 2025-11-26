package com.db.bank.app.dto;

import lombok.*;

import java.time.LocalDateTime;

public class UserDto {

    //회원가입요청 DTO
    @Getter //get 메서드 생성
    @Setter //set 메서드 생성
    @NoArgsConstructor //기본생성자 생성
    @AllArgsConstructor //모든필드 파라미터의 생성자 생성
    public static class SignupRequest{
        private String userId;
        private String password;
        private String name;
    }

    //회원가입응답 DTO
    @Getter
    @Builder //응답객체 생성
    public static class SignupResponse {
        private Long id;
        private String userId;
        private String name;
    }

    //로그인요청
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest{
        private String userId;
        private String password;
    }

    //로그인응답
    @Getter
    @Builder
    public static class LoginResponse {
        private Long id;
        private String userId;
        private String name;
    }

    //아이디로 유저정보조회 응답
    @Getter
    @Builder
    public static class UserInfoResponse{
        private Long id;
        private String userId;
        private String name;
        private LocalDateTime createdAt;
    }



}
