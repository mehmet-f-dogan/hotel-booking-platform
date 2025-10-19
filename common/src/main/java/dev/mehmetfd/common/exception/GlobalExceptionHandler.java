package dev.mehmetfd.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ErrorResponse handleBadRequest(BadRequestException ex, WebRequest request) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGeneral(Exception ex, WebRequest request) {
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }

    public record ErrorResponse(int status, String message) {
    }
}