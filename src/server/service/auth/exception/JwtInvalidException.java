package service.auth.exception;

public class JwtInvalidException extends RuntimeException {
    public JwtInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}