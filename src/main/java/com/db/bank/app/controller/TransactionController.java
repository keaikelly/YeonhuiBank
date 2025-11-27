package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.TransactionDto;
import com.db.bank.domain.entity.Transaction;
import com.db.bank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "🤝Transaction", description = "트랜잭션 관련 API")
public class TransactionController {

    private final TransactionService transactionService;


    // 1) 입금
    @PostMapping("/deposit")
    @Operation(summary = "입금")
    public ApiResponse<TransactionDto.Response> deposit(@RequestBody TransactionDto.CreateRequest req) {

        Transaction tx = transactionService.deposit(
                req.getUserId(),
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
    @PostMapping("/withdraw")
    @Operation(summary = "출금")
    public ApiResponse<TransactionDto.Response> withdraw(@RequestBody TransactionDto.CreateRequest req) {

        Transaction tx = transactionService.withdraw(
                req.getUserId(),
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
    @PostMapping("/transfer")
    @Operation(summary = "이체")
    public ApiResponse<TransactionDto.Response> transfer(@RequestBody TransactionDto.CreateRequest req) {

        Transaction tx = transactionService.transfer(
                req.getUserId(),
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
    @GetMapping("/sent")
    @Operation(summary = "내가 보낸 거래 조회")
    public ApiResponse<Page<TransactionDto.Response>> getSent(
            @RequestParam Long userId,
            @RequestParam Long fromAccountId,
            Pageable pageable
    ) {
        Page<Transaction> page = transactionService.getSentTransactions(userId, fromAccountId, pageable);

        Page<TransactionDto.Response> body = page.map(this::toResponse);
        return ApiResponse.onSuccess(Status.TRANSACTION_READ_SUCCESS, body);
    }

    // ======================================
    // 5) 내가 받은 거래 조회
    // GET /api/transactions/received?userId=&toAccountId=
    // ======================================
    @GetMapping("/received")
    @Operation(summary = "내가 받은 거래 조회")
    public ApiResponse<Page<TransactionDto.Response>> getReceived(
            @RequestParam Long userId,
            @RequestParam Long toAccountId,
            Pageable pageable
    ) {
        Page<Transaction> page = transactionService.getReceivedTransactions(userId, toAccountId, pageable);

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
