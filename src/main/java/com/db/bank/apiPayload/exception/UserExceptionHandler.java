package com.db.bank.apiPayload.exception;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler{
    @ExceptionHandler(UserException.UserNonExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleBucketListException(UserException.UserNonExistsException ex) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.USER_NON_PRESENT), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserException.InvalidLoginException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLoginException(UserException.InvalidLoginException ex) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.USER_INVALID_LOGIN), HttpStatus.UNAUTHORIZED);
    }

}
