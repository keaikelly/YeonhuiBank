package com.db.bank.apiPayload.exception;

public class AccountException extends RuntimeException {
    public AccountException(String message) {
        super(message);
    }

    public static class AccountNonExistsException extends AccountException{
        public AccountNonExistsException(String message) {
            super(message);
        }
    }
    public static class AccountAlreadyExistsException extends AccountException{
        public AccountAlreadyExistsException(String message) {
            super(message);
        }
    }
    public  static class UnauthorizedAccountAccessException extends AccountException{
        public UnauthorizedAccountAccessException(String message){
            super(message);
        }
    }
    public static class InvalidAccountAmountException extends AccountException {
        public InvalidAccountAmountException(String message) { super(message); }
    }

    public static class InvalidAccountArgumentException extends AccountException {
        public InvalidAccountArgumentException(String message) { super(message); }
    }

    public static class InsufficientBalanceException extends AccountException {
        public InsufficientBalanceException(String message) { super(message); }
    }


    
}
