package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.UserDto;
import com.db.bank.domain.entity.User;
import com.db.bank.security.CustomUserDetails;
import com.db.bank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "🤑User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;


    //유저정보조회
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ApiResponse<UserDto.UserInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        User me = userService.getById(user.getId());

        UserDto.UserInfoResponse response = UserDto.UserInfoResponse.builder()
                .id(me.getId())
                .userId(me.getUserId())
                .name(me.getName())
                .createdAt(me.getCreatedAt())
                .build();

        return ApiResponse.onSuccess(Status.USER_READ_SUCCESS, response);
    }



}
