package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.AbnTransferDto;
import com.db.bank.domain.entity.AbnTransfer;
import com.db.bank.service.AbnTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/abn-transfers")
@RequiredArgsConstructor
public class AbnTransferController {

    private final AbnTransferService abnTransferService;


    // ==========================
    // 1) 계좌 기준 이상거래 목록 조회
    // GET /api/abn-transfers/account/{accountNum}
    // ==========================
    @GetMapping("/account/{accountNum}")
    public ApiResponse<List<AbnTransferDto.Response>> getAbnTransfersByAccount(
            @PathVariable String accountNum
    ) {
        List<AbnTransfer> list =
                abnTransferService.getAllAbnTransfersByAccount(accountNum);

        List<AbnTransferDto.Response> body = list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.onSuccess(
                Status.ABN_TRANSFER_READ_SUCCESS,
                body
        );
    }

    // ==========================
    // Entity -> DTO 변환
    // ==========================
    private AbnTransferDto.Response toResponse(AbnTransfer abn) {
        return AbnTransferDto.Response.builder()
                .alertId(abn.getAlertId())
                .transactionId(abn.getTransactionId().getId())
                .accountNum(abn.getAccountNum().getAccountNum())
                .ruleCode(abn.getRuleCode())
                .detailMessage(abn.getDetailMessage())
                .createdAt(abn.getCreatedAt())
                .build();
    }
}
