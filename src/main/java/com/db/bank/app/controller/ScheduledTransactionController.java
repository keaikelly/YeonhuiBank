package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.ScheduledTransactionDto;
import com.db.bank.domain.entity.ScheduledTransaction;
import com.db.bank.domain.enums.scheduledTransaction.ScheduledStatus;
import com.db.bank.security.CustomUserDetails;
import com.db.bank.service.ScheduledTransactionService;

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
@RequestMapping("/api/scheduled-transactions")
@RequiredArgsConstructor
@Tag(name = "🕰️Scheduled Transaction", description = "예약 이체 관련 API")
public class ScheduledTransactionController {

    private final ScheduledTransactionService scheduledTransactionService;

    // ====================================
    // 1) 예약이체 생성
    // POST /api/scheduled-transactions
    // ====================================

    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    @Operation(summary = "예약이체 생성")
    public ApiResponse<ScheduledTransactionDto.Response> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody ScheduledTransactionDto.CreateRequest req) {

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
    public ApiResponse<Page<ScheduledTransactionDto.Response>> getMySchedules(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ScheduledTransaction> page = scheduledTransactionService.getMySchedules(user.getId(), pageable);

        Page<ScheduledTransactionDto.Response> body = page.map(this::toResponse);

        return ApiResponse.onSuccess(Status.SCHEDULE_READ_SUCCESS, body);
    }


    // ====================================
    // 3) 상태별 예약이체 조회
    // GET /api/scheduled-transactions/my/status?userId= &status=ACTIVE
    // ====================================
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/my/status")
    @Operation(summary = "상태별 예약이체 조회")
    public ApiResponse<Page<ScheduledTransactionDto.Response>> getMySchedulesByStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam ScheduledStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ScheduledTransaction> page = scheduledTransactionService
                .getMySchedulesByStatus(user.getId(), status, pageable);

        Page<ScheduledTransactionDto.Response> body = page.map(this::toResponse);

        return ApiResponse.onSuccess(Status.SCHEDULE_READ_SUCCESS, body);
    }


    // ====================================
    // 4) 특정 출금 계좌 기준 예약 목록
    // GET /api/scheduled-transactions/account/{fromAccountId}
    // ====================================
    @GetMapping("/account/{fromAccountId}")
    @Operation(summary = "특정 출금 계좌 기준 예약 목록")
    public ApiResponse<Page<ScheduledTransactionDto.Response>> getByFromAccount(
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
    public ApiResponse<ScheduledTransactionDto.Response> detail(
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
    public ApiResponse<ScheduledTransactionDto.Response> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId,
            @RequestBody ScheduledTransactionDto.UpdateRequest req
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
    public ApiResponse<ScheduledTransactionDto.Response> pause(
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
    public ApiResponse<ScheduledTransactionDto.Response> resume(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long scheduleId
    ) {
        scheduledTransactionService.resumeSchedule(user.getId(), scheduleId);

        ScheduledTransaction st = scheduledTransactionService.getScheduleDetail(user.getId(), scheduleId);
        return ApiResponse.onSuccess(Status.SCHEDULE_UPDATE_SUCCESS, toResponse(st));
    }


    // ====================================
    // DTO 변환 메서드
    // ====================================
    private ScheduledTransactionDto.Response toResponse(ScheduledTransaction st) {
        return ScheduledTransactionDto.Response.builder()
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
