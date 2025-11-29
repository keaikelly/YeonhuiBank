package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.ScheduledTransactionDto;
import com.db.bank.app.dto.ScheduledTransferRunDto;
import com.db.bank.domain.entity.ScheduledTransaction;
import com.db.bank.domain.entity.ScheduledTransferRun;
import com.db.bank.domain.enums.scheduledTransaction.Frequency;
import com.db.bank.domain.enums.scheduledTransaction.ScheduledStatus;
import com.db.bank.security.CustomUserDetails;
import com.db.bank.service.ScheduledTransactionService;

import com.db.bank.service.ScheduledTransferRunService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/scheduled-transactions")
@RequiredArgsConstructor
@Tag(name = "🕰️Scheduled Transaction", description = "예약 이체 관련 API")
public class ScheduledTransactionController {

    private final ScheduledTransactionService scheduledTransactionService;
    private final ScheduledTransferRunService scheduledTransferRunService;
    // ====================================
    // 1) 예약이체 생성
    // POST /api/scheduled-transactions
    // ====================================

    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    @Operation(summary = "예약이체 생성")
    public ApiResponse<ScheduledTransactionDto.ScheduledTransactionResponse> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody ScheduledTransactionDto.ScheduledTransactionCreateRequest req) {

        System.out.println("[DEBUG] startDate = " + req.getStartDate());
        System.out.println("[DEBUG] endDate   = " + req.getEndDate());
        System.out.println("[DEBUG] runTime   = " + req.getRunTime());

        ScheduledTransaction st = scheduledTransactionService.createSchedule(
                user.getId(),
                req.getFromAccountId(),
                req.getToAccountId(),
                req.getAmount(),
                req.getFrequency(),
                req.getStartDate(),
                req.getEndDate(),
                req.getRunTime(),
                req.getRrule(),
                req.getMemo()
        );

        return ApiResponse.onSuccess(Status.SCHEDULE_CREATE_SUCCESS, toResponse(st));
    }


    // ====================================
    // 2) 내가 만든 예약이체 목록
    // GET /api/scheduled-transactions/my?userId=&page=
    // ====================================
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/my")
    @Operation(summary = "내가 만든 예약이체 목록")
    public ApiResponse<Page<ScheduledTransactionDto.ScheduledTransactionResponse>> getMySchedules(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ScheduledTransaction> page = scheduledTransactionService.getMySchedules(user.getId(), pageable);

        Page<ScheduledTransactionDto.ScheduledTransactionResponse> body = page.map(this::toResponse);

        return ApiResponse.onSuccess(Status.SCHEDULE_READ_SUCCESS, body);
    }


    // ====================================
    // 3) 상태별 예약이체 조회
    // GET /api/scheduled-transactions/my/status?userId= &status=ACTIVE
    // ====================================
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/my/status")
    @Operation(summary = "상태별 예약이체 조회")
    public ApiResponse<Page<ScheduledTransactionDto.ScheduledTransactionResponse>> getMySchedulesByStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam ScheduledStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ScheduledTransaction> page = scheduledTransactionService
                .getMySchedulesByStatus(user.getId(), status, pageable);

        Page<ScheduledTransactionDto.ScheduledTransactionResponse> body = page.map(this::toResponse);

        return ApiResponse.onSuccess(Status.SCHEDULE_READ_SUCCESS, body);
    }


    // ====================================
    // 4) 특정 출금 계좌 기준 예약 목록
    // GET /api/scheduled-transactions/account/{fromAccountId}
    // ====================================
    @GetMapping("/account/{fromAccountId}")
    @Operation(summary = "특정 출금 계좌 기준 예약 목록")
    public ApiResponse<Page<ScheduledTransactionDto.ScheduledTransactionResponse>> getByFromAccount(
            @PathVariable Long fromAccountId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ScheduledTransaction> page =
                scheduledTransactionService.getSchedulesByFromAccount(fromAccountId, pageable);

        return ApiResponse.onSuccess(
                Status.SCHEDULE_READ_SUCCESS,
                page.map(this::toResponse)
        );
    }


    // ====================================
    // 5) 예약이체 단건 조회
    // GET /api/scheduled-transactions/{scheduleId}?userId=
    // ====================================

    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{scheduleId}")
    @Operation(summary = "예약 이체 단건 조회")
    public ApiResponse<ScheduledTransactionDto.ScheduledTransactionResponse> detail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId
    ) {
        ScheduledTransaction st = scheduledTransactionService.getScheduleDetail(user.getId(), scheduleId);

        return ApiResponse.onSuccess(
                Status.SCHEDULE_READ_SUCCESS,
                toResponse(st)
        );
    }


    // ====================================
    // 6) 예약이체 수정
    // PATCH /api/scheduled-transactions/{scheduleId}?userId=
    // ====================================

    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/{scheduleId}")
    @Operation(summary = "예약 이체 수정")
    public ApiResponse<ScheduledTransactionDto.ScheduledTransactionResponse> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId,
            @RequestBody ScheduledTransactionDto.ScheduledTransactionUpdateRequest req
    ) {
        ScheduledTransaction st = scheduledTransactionService.updateSchedule(
                user.getId(),
                scheduleId,
                req.getAmount(),
                req.getFrequency(),
                req.getStartDate(),
                req.getEndDate(),
                req.getMemo()
        );

        return ApiResponse.onSuccess(Status.SCHEDULE_UPDATE_SUCCESS, toResponse(st));
    }


    // ====================================
    // 7) 예약이체 취소
    // DELETE /api/scheduled-transactions/{scheduleId}?userId=
    // ====================================

    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "예약 이체 취소")
    public ApiResponse<String> cancel(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId
    ) {
        scheduledTransactionService.cancelSchedule(user.getId(), scheduleId);

        return ApiResponse.onSuccess(Status.SCHEDULE_CANCEL_SUCCESS, "취소 완료");
    }


    // ====================================
    // 8) 예약이체 일시정지
    // POST /api/scheduled-transactions/{scheduleId}/pause?userId=
    // ====================================
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{scheduleId}/pause")
    @Operation(summary = "예약 이체 일시 정지")
    public ApiResponse<ScheduledTransactionDto.ScheduledTransactionResponse> pause(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId
    ) {
        scheduledTransactionService.pauseSchedule(user.getId(), scheduleId);

        ScheduledTransaction st = scheduledTransactionService.getScheduleDetail(user.getId(), scheduleId);
        return ApiResponse.onSuccess(Status.SCHEDULE_UPDATE_SUCCESS, toResponse(st));
    }

    // ====================================
    // 9) 예약이체 재개
    // POST /api/scheduled-transactions/{scheduleId}/resume?userId=
    // ====================================
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{scheduleId}/resume")
    @Operation(summary = "예약 이체 재개")
    public ApiResponse<ScheduledTransactionDto.ScheduledTransactionResponse> resume(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId
    ) {
        scheduledTransactionService.resumeSchedule(user.getId(), scheduleId);

        ScheduledTransaction st = scheduledTransactionService.getScheduleDetail(user.getId(), scheduleId);
        return ApiResponse.onSuccess(Status.SCHEDULE_UPDATE_SUCCESS, toResponse(st));
    }
    // ====================================
// 10) 예약이체 즉시 실행 (데모/재시도 용)
// POST /api/scheduled-transactions/{scheduleId}/run-now
// ====================================
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{scheduleId}/run-now")
    @Operation(summary = "예약이체 즉시 실행(재시도/강제 실행)", description = "스케줄러를 기다리지 않고 바로 예약이체를 1회 실행합니다.")
    public ApiResponse<String> runNow(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId
    ) {

        // 일정 권한/소유자 체크
        scheduledTransactionService.runNow(user.getId(), scheduleId);

        return ApiResponse.onSuccess(
                Status.SCHEDULE_RUN_NOW_SUCCESS,
                "예약이체가 즉시 실행되었습니다."
        );
    }

    // ====================================
    // 11) 특정 예약이체의 실패 실행 로그 조회
    // GET /api/scheduled-transactions/{scheduleId}/runs/failures
    // ====================================
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{scheduleId}/runs/failures")
    @Operation(summary = "예약이체 실패 실행 로그 조회",
            description = "해당 예약이체에 대해 지금까지 실패한 실행(run) 내역과 실패 사유를 조회합니다.")
    public ApiResponse<List<ScheduledTransferRunDto.FailureResponse>> getFailures(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId
    ) {
        List<ScheduledTransferRun> runs =
                scheduledTransferRunService.getMyFailures(user.getId(), scheduleId);

        List<ScheduledTransferRunDto.FailureResponse> body = runs.stream()
                .map(this::toFailureResponse)
                .toList();

        return ApiResponse.onSuccess(Status.SCHEDULE_RUN_FAILURE_READ_SUCCESS, body);
    }

    // ========= 실행로그 DTO 변환 =========
    private ScheduledTransferRunDto.FailureResponse toFailureResponse(ScheduledTransferRun run) {
        return ScheduledTransferRunDto.FailureResponse.builder()
                .runId(run.getId())
                .scheduleId(run.getSchedule().getId())
                .executedAt(run.getExecutedAt())
                .runTime(run.getRunTime() != null ? run.getRunTime().toString() : null)
                .result(run.getResult())
                .message(run.getMessage())
                .failureReasonCode(
                        run.getFailureReason() != null ? run.getFailureReason().getReasonCode() : null
                )
                .failureReasonDesc(
                        run.getFailureReason() != null ? run.getFailureReason().getDescription() : null
                )
                .retryNo(run.getRetryNo())
                .maxRetries(run.getMaxRetries())
                .nextRetryAt(run.getNextRetryAt())
                .build();
    }



    // ====================================
    // DTO 변환 메서드
    // ====================================
    private ScheduledTransactionDto.ScheduledTransactionResponse toResponse(ScheduledTransaction st) {
        return ScheduledTransactionDto.ScheduledTransactionResponse.builder()
                .scheduleId(st.getId())
                .fromAccountId(st.getFromAccount().getId())
                .toAccountId(st.getToAccount().getId())
                .createdBy(st.getCreatedBy().getId())
                .amount(st.getAmount())
                .scheduledStatus(st.getScheduledStatus())
                .frequency(st.getFrequency())
                .rrule(st.getRrule())
                .startDate(st.getStartDate())
                .endDate(st.getEndDate())
                .nextRunAt(st.getNextRunAt())
                .lastRunAt(st.getLastRunAt())
                .createdAt(st.getCreatedAt())
                .updatedAt(st.getUpdatedAt())
                .runTime(st.getRunTime())
                .memo(st.getMemo())
                .build();
    }
}
