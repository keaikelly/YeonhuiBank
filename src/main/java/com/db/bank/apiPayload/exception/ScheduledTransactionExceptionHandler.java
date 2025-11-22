package com.db.bank.apiPayload.exception;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ScheduledTransactionExceptionHandler {
    @ExceptionHandler(ScheduledTransactionException.InvalidScheduledTransactionAmountException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.InvalidScheduledTransactionAmountException ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.INVALID_SCHEDULED_TRANSACTION_AMOUNT),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ScheduledTransactionException.InvalidScheduledTransactionStartDateException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.InvalidScheduledTransactionStartDateException ex) {
            return new ResponseEntity<>(
                    ApiResponse.onFailure(Status.INVALID_SCHEDULED_TRANSACTION_STARTDATE),
                    HttpStatus.BAD_REQUEST

            );
    }
    @ExceptionHandler(ScheduledTransactionException.ScheduledTransactionAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.ScheduledTransactionAlreadyExistsException ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.INVALID_SCHEDULED_TRANSACTION_STARTDATE),
                HttpStatus.CONFLICT

        );
    }
    @ExceptionHandler(ScheduledTransactionException.UnauthorizedScheduledTransaction.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.UnauthorizedScheduledTransaction ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.SCHEDULED_TRANSACTION_FORBIDDEN),
                HttpStatus.FORBIDDEN

        );
    }
    @ExceptionHandler(ScheduledTransactionException.InvalidScheduledTransactionTimeException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.InvalidScheduledTransactionTimeException ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.INVALID_SCHEDULED_TRANSACTION_TIME),
                HttpStatus.BAD_REQUEST

        );
    }
    @ExceptionHandler(ScheduledTransactionException.ScheduledTransactionAlreadyFinishedException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.ScheduledTransactionAlreadyFinishedException ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.SCHEDULED_TRANSACTION_ALREADY_FINISHED),
                HttpStatus.CONFLICT

        );
    }
    @ExceptionHandler(ScheduledTransactionException.InvalidScheduleStatusForPauseException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.InvalidScheduleStatusForPauseException ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.INVALID_SCHEDULE_STATUS_FOR_PAUSE),
                HttpStatus.CONFLICT

        );
    }
    @ExceptionHandler(ScheduledTransactionException.InvalidScheduleStatusForResumeException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.InvalidScheduleStatusForResumeException ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.INVALID_SCHEDULE_STATUS_FOR_RESUME),
                HttpStatus.CONFLICT

        );
    }
    @ExceptionHandler(ScheduledTransactionException.ScheduledTransactionNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(ScheduledTransactionException.ScheduledTransactionNotFoundException ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.SCHEDULED_TRANSACTION_NOT_FOUND),
                HttpStatus.NOT_FOUND

        );
    }
}