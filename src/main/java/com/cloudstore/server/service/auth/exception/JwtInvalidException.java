package com.cloudstore.server.service.auth.exception;

public class JwtInvalidException extends RuntimeException {

    /**
        * Constructs a new JwtInvalidException with the specified detail message.
        * @param message The detail message.
    **/
    public JwtInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}