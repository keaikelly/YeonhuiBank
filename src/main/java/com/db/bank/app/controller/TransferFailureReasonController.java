package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.TransferFailureReasonDto;
import com.db.bank.domain.entity.TransferFailureReason;
import com.db.bank.service.TransferFailureReasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/failure-reasons")
@Tag(name = "❌Transfer Failure Reason", description = "이체 실패 사유 코드 관리 API")
public class TransferFailureReasonController {

    private final TransferFailureReasonService transferFailureReasonService;

    // ==========================
    // 1) 실패 사유 등록 (관리자용)
    // ==========================
    @PostMapping
    @Operation(summary = "이체 실패 사유 등록")
    public ApiResponse<TransferFailureReasonDto.CreateFailureReasonResponse> createFailureReason(
            @RequestBody TransferFailureReasonDto.CreateFailureReasonRequest request
    ) {
        TransferFailureReason reason = transferFailureReasonService.createReason(
                request.getReasonCode(),
                request.getDescription()
        );

        TransferFailureReasonDto.CreateFailureReasonResponse response =
                TransferFailureReasonDto.CreateFailureReasonResponse.builder()
                        .reasonCode(reason.getReasonCode())
                        .description(reason.getDescription())
                        .build();

        return ApiResponse.onSuccess(Status.REASON_CREATE_SUCCESS, response);
    }

    // ==========================
    // 2) 실패 사유 단건 조회
    // ==========================
    @GetMapping("/{reasonCode}")
    @Operation(summary = "이체 실패 사유 단건 조회")
    public ApiResponse<TransferFailureReasonDto.GetFailureReasonResponse> getFailureReason(
            @PathVariable String reasonCode
    ) {
        TransferFailureReason reason = transferFailureReasonService.getReason(reasonCode);

        TransferFailureReasonDto.GetFailureReasonResponse response =
                TransferFailureReasonDto.GetFailureReasonResponse.builder()
                        .reasonCode(reason.getReasonCode())
                        .description(reason.getDescription())
                        .build();

        return ApiResponse.onSuccess(Status.REASON_GET_SUCCESS, response);
    }
}
