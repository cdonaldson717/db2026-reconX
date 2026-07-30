package com.dbtraining.reconx.exception;

/** Raised when login credentials are missing, unknown, disabled, or incorrect. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
