package com.db.bank.apiPayload.exception;

public class ScheduledTransactionException extends RuntimeException{
    public ScheduledTransactionException(String message) {
        super(message);
    }

    public static class InvalidScheduledTransactionAmountException extends ScheduledTransactionException{
        public InvalidScheduledTransactionAmountException(String message) {
            super(message);
        }
    }
    public static class InvalidScheduledTransactionStartDateException extends ScheduledTransactionException{
        public InvalidScheduledTransactionStartDateException(String message) {
            super(message);
        }
    }
    public static class ScheduledTransactionAlreadyExistsException extends ScheduledTransactionException {

        public ScheduledTransactionAlreadyExistsException(String message) {
            super(message);
        }
    }
    public static class InvalidScheduledTransactionTimeException extends ScheduledTransactionException {
        public InvalidScheduledTransactionTimeException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedScheduledTransaction extends ScheduledTransactionException {

        public UnauthorizedScheduledTransaction(String message) {
            super(message);
        }
    }
    public static class ScheduledTransactionAlreadyFinishedException extends ScheduledTransactionException {
        public ScheduledTransactionAlreadyFinishedException(String message) {
            super(message);
        }
    }
    public static class InvalidScheduleStatusForPauseException extends ScheduledTransactionException {
        public InvalidScheduleStatusForPauseException(String message) {
            super(message);
        }
    }
    public static class InvalidScheduleStatusForResumeException extends ScheduledTransactionException {
        public InvalidScheduleStatusForResumeException(String message) {
            super(message);
        }
    }
    public static class ScheduledTransactionNotFoundException extends ScheduledTransactionException {
        public ScheduledTransactionNotFoundException(String message) {
            super(message);
        }
    }
    public static class ScheduledTransactionAreadyRunningException extends ScheduledTransactionException {
        public ScheduledTransactionAreadyRunningException(String message) {
            super(message);
        }
    }




}
