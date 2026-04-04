package com.cloudstore.server.service.auth.exception;

public class JwtExpiredException extends RuntimeException {

    /**
        * Constructs a new JwtExpiredException with the specified detail message.
        * @param message The detail message.
    **/
    public JwtExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}