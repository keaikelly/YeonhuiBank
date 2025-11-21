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

    
}
