package com.cloudstore.server.messaging.exception;

public class MessagingException extends RuntimeException {
    public MessagingException(String message) {
        super(message);
    }
    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}