package com.cloudstore.server.service.exception;

public class ResourceNotFoundException extends ServiceException {

    /**
        * Constructs a new ResourceNotFoundException with the specified detail message.
        * @param message The detail message.
    **/
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
        * Constructs a new ResourceNotFoundException with the specified detail message and cause.
        * @param message The detail message.
        * @param cause The cause of the exception.
    **/
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
