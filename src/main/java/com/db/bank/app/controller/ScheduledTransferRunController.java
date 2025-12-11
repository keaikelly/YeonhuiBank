package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.ScheduledTransferRunDto;
import com.db.bank.domain.entity.ScheduledTransferRun;
import com.db.bank.domain.entity.TransferFailureReason;
import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import com.db.bank.security.CustomUserDetails;
import com.db.bank.service.ScheduledTransferRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduled-transfer-runs")
@RequiredArgsConstructor
@Tag(name = "⏰📋Scheduled Transfer Run (로그)", description = "예약 이체 로그 관련 API")
public class ScheduledTransferRunController {

    private final ScheduledTransferRunService scheduledTransferRunService;

    /**
     * 특정 예약이체의 실행 이력 조회
     * 전체 실행 내역 조회 - /api/scheduled-transfer-runs/schedule/{scheduleId}
     * 특정 결과만 보고 싶으면  - /api/scheduled-transfer-runs/schedule/{scheduleId}?result=SUCCESS
     */
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/schedule/{scheduleId}")
    @Operation(summary = "특정 예약이체의 실행 이력 조회(전체 혹은 특정 결과만)")
    public ApiResponse<List<ScheduledTransferRunDto.ScheduledTransferRunResponse>> getRunsBySchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) RunResult result,
            @ParameterObject
            @PageableDefault(sort = "executedAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Long userId = user.getId();
        List<ScheduledTransferRun> runs ;

        if (result == null) {
            // 전체 실행 이력
            runs = scheduledTransferRunService.getRunsBySchedule(userId, scheduleId, pageable);
        } else {
            // 결과별 필터 (SUCCESS, ERROR, INSUFFICIENT_FUNDS 등)
            runs = scheduledTransferRunService.getRunsByScheduleAndResult(userId, scheduleId, result, pageable);
        }


        List<ScheduledTransferRunDto.ScheduledTransferRunResponse> body = runs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.onSuccess(Status.SCHEDULE_RUN_READ_SUCCESS, body);
    }

    // ==========================
    // Entity → DTO 변환
    // ==========================
    private ScheduledTransferRunDto.ScheduledTransferRunResponse toResponse(ScheduledTransferRun run) {
        TransferFailureReason reason = run.getFailureReason();
        return ScheduledTransferRunDto.ScheduledTransferRunResponse.builder()
                .runId(run.getId())
                .scheduleId(run.getSchedule().getId())
                .executedAt(run.getExecutedAt())
                .result(run.getResult())
                .message(run.getMessage())
                .txnOutId(run.getTxnOut() != null ? run.getTxnOut().getId() : null)
                .txnInId(run.getTxnIn() != null ? run.getTxnIn().getId() : null)
                .failureReasonCode(reason != null ? reason.getReasonCode() : null)
                .failureReasonDesc(reason != null ? reason.getDescription() : null)
                .maxRetries(run.getMaxRetries())
                .nextRetryAt(run.getNextRetryAt())
                .build();
    }
}
