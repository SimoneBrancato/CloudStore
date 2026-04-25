package com.cloudstore.server.service.exception;

public class UnauthorizedException extends ServiceException {

    /**
        * Constructs a new UnauthorizedException with the specified detail message.
        * @param message The detail message.
    **/
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
        * Constructs a new UnauthorizedException with the specified detail message and cause.
        * @param message The detail message.
        * @param cause The cause of the exception.
    **/
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
