package com.db.bank.app.dto;

import lombok.*;

public class TransferFailureReasonDto {

    //실패사유 등록요청 Dto
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateFailureReasonRequest {
        private String reasonCode;
        private String description;
    }

    //실패사유 등록응답 Dto
    @Getter
    @Builder
    public static class CreateFailureReasonResponse {
        private String reasonCode;
        private String description;
    }

    //실패사유 단건조회 응답 Dto
    @Getter
    @Builder
    public static class GetFailureReasonResponse {
        private String reasonCode;
        private String description;
    }

}
