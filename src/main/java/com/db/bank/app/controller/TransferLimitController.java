package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.TransferLimitDto;
import com.db.bank.domain.entity.TransferLimit;
import com.db.bank.security.CustomUserDetails;
import com.db.bank.service.TransferLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfer-limits")
@RequiredArgsConstructor
@Tag(name = "⛔️Transfer Limit", description = "이체한도 관련 API")
@SecurityRequirement(name = "BearerAuth")   // 이 클래스 전체가 JWT 필요
public class TransferLimitController {

    private final TransferLimitService transferLimitService;

    // ==========================
    // 1) 이체한도 등록/변경
    // ==========================
    @PostMapping
    @Operation(summary = "이체한도 등록/변경")
    public ApiResponse<TransferLimitDto.Response> createOrUpdateLimit(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody TransferLimitDto.CreateRequest request
    ) {
        TransferLimit limit = transferLimitService.createTransferLimit(
                user.getId(),                 // 토큰에서 가져온 유저
                request.getAccountNum(),
                request.getDailyLimitAmt(),
                request.getPerTxLimitAmt(),
                request.getNote()
        );

        return ApiResponse.onSuccess(
                Status.TRANSFER_LIMIT_CREATE_SUCCESS,
                toResponse(limit)
        );
    }

    // ==========================
    // 2) 특정 계좌의 활성 이체한도 조회
    // ==========================
    @GetMapping("/active/{accountNum}")
    @Operation(summary = "특정 계좌의 활성 이체한도 조회")
    public ApiResponse<List<TransferLimitDto.Response>> getActiveLimits(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String accountNum
    ) {
        List<TransferLimit> limits =
                transferLimitService.getActiveTransferLimit(user.getId(), accountNum);

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
    // ==========================
    @GetMapping("/history/{accountNum}")
    @Operation(summary = "특정 계좌의 이체한도 이력(전체) 조회")
    public ApiResponse<List<TransferLimitDto.Response>> getHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String accountNum
    ) {
        List<TransferLimit> limits =
                transferLimitService.getPastTransferLimits(user.getId(), accountNum);

        List<TransferLimitDto.Response> body = limits.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.onSuccess(
                Status.TRANSFER_LIMIT_HISTORY_READ_SUCCESS,
                body
        );
    }

    // ==========================
    // 4) endDate 수정
    // ==========================
    @PatchMapping("/{limitId}/end-date")
    @Operation(summary = "이체한도 endDate 수정")
    public ApiResponse<TransferLimitDto.Response> updateEndDate(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long limitId,
            @RequestBody TransferLimitDto.UpdateEndDateRequest request
    ) {
        TransferLimit limit =
                transferLimitService.updateEndDate(user.getId(), limitId, request.getEndDate());

        return ApiResponse.onSuccess(
                Status.TRANSFER_LIMIT_ENDDATE_UPDATE_SUCCESS,
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
