package com.cloudstore.server.service.exception;

public class ServiceException extends Exception {

    /**
        * Constructs a new ServiceException with the specified detail message.
        * @param message The detail message.
    **/
    public ServiceException(String message) {
        super(message);
    }

    /**
        * Constructs a new ServiceException with the specified detail message and cause.
        * @param message The detail message.
        * @param cause The cause of the exception.
    **/
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
