package com.cloudstore.server.service.exception;

public class ValidationException extends ServiceException {

    /**
        * Constructs a new ValidationException with the specified detail message.
        * @param message The detail message.
    **/
    public ValidationException(String message) {
        super(message);
    }

    /**
        * Constructs a new ValidationException with the specified detail message and cause.
        * @param message The detail message.
        * @param cause The cause of the exception.
    **/
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
