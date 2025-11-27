package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.TransferLimitDto;
import com.db.bank.domain.entity.TransferLimit;
import com.db.bank.service.TransferLimitService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfer-limits")
@RequiredArgsConstructor
@Tag(name = "⛔️Transfer Limit", description = "이체한도 관련 API")
public class TransferLimitController {

    private final TransferLimitService transferLimitService;

    // ==========================
    // 1) 이체한도 등록/변경
    // POST /api/transfer-limits
    // ==========================
    @PostMapping
    @Operation(summary = "이체한도 등록/변경")
    public ApiResponse<TransferLimitDto.Response> createOrUpdateLimit(
            @RequestBody TransferLimitDto.CreateRequest request
    ) {
        TransferLimit limit = transferLimitService.createTransferLimit(
                request.getAccountNum(),
                request.getDailyLimitAmt(),
                request.getPerTxLimitAmt(),
                request.getNote()
        );

        return ApiResponse.onSuccess(
                Status.TRANSFER_LIMIT_CREATE_SUCCESS, // Status enum에 추가해서 사용
                toResponse(limit)
        );
    }

    // ==========================
    // 2) 특정 계좌의 활성 이체한도 조회
    // GET /api/transfer-limits/active/{accountNum}
    // ==========================
    @GetMapping("/active/{accountNum}")
    @Operation(summary = "특정 계좌의 활성 이체한도 조회")
    public ApiResponse<List<TransferLimitDto.Response>> getActiveLimits(
            @PathVariable String accountNum
    ) {
        List<TransferLimit> limits = transferLimitService.getActiveTransferLimit(accountNum);

        List<TransferLimitDto.Response> body = limits.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.onSuccess(
                Status.TRANSFER_LIMIT_READ_SUCCESS,
                body
        );
    }

    // ==========================
    // 3) 특정 계좌의 이체한도 이력(전체) 조회
    // GET /api/transfer-limits/history/{accountNum}
    // ==========================
    @GetMapping("/history/{accountNum}")
    @Operation(summary = "특정 계좌의 이체한도 이력(전체) 조회")
    public ApiResponse<List<TransferLimitDto.Response>> getHistory(
            @PathVariable String accountNum
    ) {
        List<TransferLimit> limits = transferLimitService.getPastTransferLimits(accountNum);

        List<TransferLimitDto.Response> body = limits.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.onSuccess(
                Status.TRANSFER_LIMIT_HISTORY_READ_SUCCESS,
                body
        );
    }
    @PatchMapping("/{limitId}/end-date")
    @Operation(summary = "endDate 수정 조회")
    public ApiResponse<TransferLimitDto.Response> updateEndDate(
            @PathVariable Long limitId,
            @RequestBody TransferLimitDto.UpdateEndDateRequest request
    ) {
        TransferLimit limit = transferLimitService.updateEndDate(limitId, request.getEndDate());

        return ApiResponse.onSuccess(
                Status.TRANSFER_LIMIT_ENDDATE_UPDATE_SUCCESS,  // Status enum에 추가
                toResponse(limit)
        );
    }

    // ==========================
    // Entity -> DTO 변환
    // ==========================
    private TransferLimitDto.Response toResponse(TransferLimit limit) {
        return TransferLimitDto.Response.builder()
                .limitId(limit.getId())
                .accountNum(limit.getAccount().getAccountNum())
                .dailyLimitAmt(limit.getDailyLimitAmt())
                .perTxLimitAmt(limit.getPerTxLimitAmt())
                .startDate(limit.getStartDate())
                .endDate(limit.getEndDate())
                .status(limit.getStatus())
                .note(limit.getNote())
                .createdAt(limit.getCreatedAt())
                .updatedAt(limit.getUpdatedAt())
                .build();
    }



}
