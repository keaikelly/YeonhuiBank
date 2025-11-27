package com.db.bank.app.controller;


import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.AccountDto;
import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.domain.entity.Account;
import com.db.bank.security.CustomUserDetails;
import com.db.bank.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "🏦Account", description = "계좌 관련 API")
public class AccountController {

    private final AccountService accountService;

    // ==========================
    // 1) 계좌 생성
    // ==========================
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    @Operation(summary = "계좌 생성")
    public ApiResponse<AccountDto.CreateResponse> createAccount(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody AccountDto.CreateRequest request
    ) {
        Account account = accountService.createAccount(
                user.getId(),
                request.getAccountNum(),
                request.getAccountType(),
                request.getInitialBalance()
        );

        AccountDto.CreateResponse response = AccountDto.CreateResponse.builder()
                .accountId(account.getId())
                .accountNum(account.getAccountNum())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .userId(account.getUser().getId())
                .build();

        return ApiResponse.onSuccess(Status.ACCOUNT_CREATE_SUCCESS, response);
    }

    // ==========================
    // 2) 특정 유저 계좌 목록 조회
    // ==========================
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/me")
    @Operation(summary = "특정 유저 계좌 목록 조회")
    public ApiResponse<Page<AccountDto.DetailResponse>> getUserAccounts(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AccountDto.DetailResponse> response = accountService.getAccountsByUser(user.getId(), pageable)
                .map(acc -> AccountDto.DetailResponse.builder()
                        .accountId(acc.getId())
                        .accountNum(acc.getAccountNum())
                        .accountType(acc.getAccountType())
                        .balance(acc.getBalance())
                        .createdAt(acc.getCreatedAt())
                        .userId(acc.getUser().getId())
                        .build()
                );

        return ApiResponse.onSuccess(Status.ACCOUNT_READ_SUCCESS, response);
    }

    // ==========================
    // 3) 단일 계좌 조회 + 소유자 검증
    // ==========================
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{accountNum}")
    @Operation(summary = "단일 계좌 조회(소유자 검증)")
    public ApiResponse<AccountDto.DetailResponse> getAccountDetail(
            @PathVariable String accountNum,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Account account = accountService.getAccountForUser(accountNum, user.getId());

        AccountDto.DetailResponse response = AccountDto.DetailResponse.builder()
                .accountId(account.getId())
                .accountNum(account.getAccountNum())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .userId(account.getUser().getId())
                .build();

        return ApiResponse.onSuccess(Status.ACCOUNT_READ_SUCCESS ,response);
    }
}
