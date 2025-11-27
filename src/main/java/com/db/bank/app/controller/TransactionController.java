package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.TransactionDto;
import com.db.bank.domain.entity.Transaction;
import com.db.bank.security.CustomUserDetails;
import com.db.bank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "🤝Transaction", description = "트랜잭션 관련 API")
public class TransactionController {

    private final TransactionService transactionService;


    // 1) 입금
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/deposit")
    @Operation(summary = "입금")
    public ApiResponse<TransactionDto.Response> deposit(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody TransactionDto.CreateRequest req) {

        Transaction tx = transactionService.deposit(
                user.getId(),
                req.getToAccountNum(),   // 입금 계좌
                req.getAmount(),
                req.getMemo()
        );

        return ApiResponse.onSuccess(
                Status.TRANSACTION_CREATE_SUCCESS,
                toResponse(tx)
        );
    }


    // 2) 출금
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/withdraw")
    @Operation(summary = "출금")
    public ApiResponse<TransactionDto.Response> withdraw(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody TransactionDto.CreateRequest req) {

        Transaction tx = transactionService.withdraw(
                user.getId(),
                req.getFromAccountNum(), // 출금 계좌
                req.getAmount(),
                req.getMemo()
        );

        return ApiResponse.onSuccess(
                Status.TRANSACTION_CREATE_SUCCESS,
                toResponse(tx)
        );
    }


    // 3) 이체

    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/transfer")
    @Operation(summary = "이체")
    public ApiResponse<TransactionDto.Response> transfer(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody TransactionDto.CreateRequest req) {

        Transaction tx = transactionService.transfer(
                user.getId(),
                req.getFromAccountNum(),
                req.getToAccountNum(),
                req.getAmount(),
                req.getMemo()
        );

        return ApiResponse.onSuccess(
                Status.TRANSACTION_CREATE_SUCCESS,
                toResponse(tx)
        );
    }


    // 4) 내가 보낸 거래 조회
    // GET /api/transactions/sent?userId=&fromAccountId=
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/sent")
    @Operation(summary = "내가 보낸 거래 조회")
    public ApiResponse<Page<TransactionDto.Response>> getSent(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long fromAccountId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Transaction> page = transactionService.getSentTransactions(user.getId(), fromAccountId, pageable);

        Page<TransactionDto.Response> body = page.map(this::toResponse);
        return ApiResponse.onSuccess(Status.TRANSACTION_READ_SUCCESS, body);
    }

    // ======================================
    // 5) 내가 받은 거래 조회
    // GET /api/transactions/received?userId=&toAccountId=
    // ======================================
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/received")
    @Operation(summary = "내가 받은 거래 조회")
    public ApiResponse<Page<TransactionDto.Response>> getReceived(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long toAccountId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Transaction> page = transactionService.getReceivedTransactions(user.getId(), toAccountId, pageable);

        Page<TransactionDto.Response> body = page.map(this::toResponse);
        return ApiResponse.onSuccess(Status.TRANSACTION_READ_SUCCESS, body);
    }


    // ======================================
    // 내부 변환 메서드
    // ======================================
    private TransactionDto.Response toResponse(Transaction tx) {
        return TransactionDto.Response.builder()
                .transactionId(tx.getId())
                .fromAccountId(tx.getFromAccount().getId())
                .toAccountId(tx.getToAccount().getId())
                .type(tx.getType())
                .status(tx.getStatus())
                .amount(tx.getAmount())
                .memo(tx.getMemo())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
