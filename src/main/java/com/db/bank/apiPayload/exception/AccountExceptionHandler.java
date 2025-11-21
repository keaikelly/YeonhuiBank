package com.db.bank.apiPayload.exception;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class AccountExceptionHandler {
    @ExceptionHandler(AccountException.AccountNonExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleBucketListExcption(AccountException.AccountNonExistsException ex) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.ACCOUNT_NON_PRESENT), HttpStatus.NOT_FOUND);
    }
}
