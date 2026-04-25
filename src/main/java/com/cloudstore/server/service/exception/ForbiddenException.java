package com.cloudstore.server.service.exception;

public class ForbiddenException extends ServiceException {

    /**
        * Constructs a new ForbiddenException with the specified detail message.
        * @param message The detail message.
    **/
    public ForbiddenException(String message) {
        super(message);
    }

    /**
        * Constructs a new ForbiddenException with the specified detail message and cause.
        * @param message The detail message.
        * @param cause The cause of the exception.
    **/
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
