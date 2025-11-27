package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.UserDto;
import com.db.bank.domain.entity.User;
import com.db.bank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "🤑User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    //회원가입 중 아이디 중복확인
    @GetMapping("/check")
    @Operation(summary = "회원가입 중 아이디 중복확인")
    public ApiResponse<Boolean> checkId(@RequestParam String userId) {
        boolean exists=userService.checkId(userId);
        return  ApiResponse.onSuccess(Status.USER_CHECK_ID, exists);
    }

    //회원가입
    @PostMapping("/signup") //post 요청
    @Operation(summary = "회원가입")
    public ApiResponse<UserDto.SignupResponse> signup(@RequestBody UserDto.SignupRequest request ) {
        User user =userService.createUser(
                request.getUserId(),
                request.getPassword(),
                request.getName()
        );
        UserDto.SignupResponse response = UserDto.SignupResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .build();

        return ApiResponse.onSuccess(Status.USER_SIGNUP_SUCCESS, response);
    }

    //로그인
    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ApiResponse<UserDto.LoginResponse> login(@RequestBody UserDto.LoginRequest request ) {
        User user =userService.login(request.getUserId(), request.getPassword());

        UserDto.LoginResponse response=UserDto.LoginResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .build();

        return ApiResponse.onSuccess(Status.USER_LOGIN_SUCCESS, response);
    }

    //유저정보조회
    @GetMapping("/{userId}")
    @Operation(summary = "유저정보조회")
    public ApiResponse<UserDto.UserInfoResponse> getUserInfo(@PathVariable String userId) {
        User user=userService.getUserById(userId);

        UserDto.UserInfoResponse response =UserDto.UserInfoResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
        return ApiResponse.onSuccess(Status.USER_READ_SUCCESS, response);
    }

}
