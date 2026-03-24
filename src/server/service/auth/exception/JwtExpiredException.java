package service.auth.exception;

public class JwtExpiredException extends RuntimeException {
    public JwtExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}