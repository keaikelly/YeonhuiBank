package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.LogDto;
import com.db.bank.domain.entity.Log;
import com.db.bank.domain.enums.log.Action;
import com.db.bank.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "📝Log", description = "로그 관련 API")
public class LogController {

    private final LogService logService;

    //계좌 별 로그 조회
    @GetMapping("/account/{accountNum}")
    @Operation(summary = "계좌 별 로그 조회")
    public ApiResponse<Page<LogDto.LogResponse>> getLogsByAccount(
            @PathVariable String accountNum,
            Pageable pageable
    ) {
        Page<Log> logs = logService.getLogsByAccount(accountNum, pageable);

        Page<LogDto.LogResponse> body = logs.map(this::toLogResponse);

        return ApiResponse.onSuccess(Status.LOG_READ_SUCCESS, body);
    }

    //사용자 별 로그 조회
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 별 로그 조회")
    public ApiResponse<Page<LogDto.LogResponse>> getLogsByActorUser(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        Page<Log> logs = logService.getLogsByActorUser(userId, pageable);

        Page<LogDto.LogResponse> body = logs.map(this::toLogResponse);

        return ApiResponse.onSuccess(Status.LOG_READ_SUCCESS, body);
    }


    // 계좌 + 기간 로그 조회
    @GetMapping("/account/{accountNum}/period")
    @Operation(summary = "계좌 + 기간 로그 조회")
    public ApiResponse<Page<LogDto.LogResponse>> getLogsByAccountAndPeriod(
            @PathVariable String accountNum,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,
            Pageable pageable
    ) {
        Page<Log> logs = logService.getLogsByAccountAndPeriod(accountNum, start, end, pageable);

        Page<LogDto.LogResponse> body = logs.map(this::toLogResponse);

        return ApiResponse.onSuccess(Status.LOG_READ_SUCCESS, body);
    }


    // 4) 액션 타입별 로그 조회 ex DEPOSIT

    @GetMapping("/action/{action}")
    @Operation(summary = "액션 타입별 로그 조회")
    public ApiResponse<Page<LogDto.LogResponse>> getLogsByAction(
            @PathVariable Action action,
            Pageable pageable
    ) {
        Page<Log> logs = logService.getLogsByAction(action, pageable);

        Page<LogDto.LogResponse> body = logs.map(this::toLogResponse);

        return ApiResponse.onSuccess(Status.LOG_READ_SUCCESS, body);
    }


    // 내부 변환 메서드
    private LogDto.LogResponse toLogResponse(Log log) {
        return LogDto.LogResponse.builder()
                .logId(log.getId())
                .transactionId(log.getTransaction().getId())
                .accountNum(log.getAccount().getAccountNum())
                .actorUserId(log.getActorUser().getId())
                .beforeBalance(log.getBeforeBalance())
                .afterBalance(log.getAfterBalance())
                .action(log.getAction())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
