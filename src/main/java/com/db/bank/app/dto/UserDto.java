package com.db.bank.app.dto;

import lombok.*;

import java.time.LocalDateTime;

public class UserDto {



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
