package com.db.bank.app.dto;

import lombok.*;

public class AuthDto {

    @Getter @Setter
    public static class SignupRequest {
        private String name;
        private String userId;   // 로그인 아이디
        private String password;
    }

    @Getter @Setter
    public static class LoginRequest {
        private String userId;   // 로그인 아이디
        private String password;
    }

    @Getter @Builder
    public static class LoginResponse {
        private String token;
        private Long userPk;
        private String userId;
        private String name;
    }
}
