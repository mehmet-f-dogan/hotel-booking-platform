package dev.mehmetfd.common.exception;

public class TryLaterException extends RuntimeException {

    public TryLaterException() {
        super("Service temporarily unavailable. Please try again later.");
    }
}