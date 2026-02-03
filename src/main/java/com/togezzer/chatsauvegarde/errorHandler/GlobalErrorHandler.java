package com.togezzer.chatsauvegarde.errorHandler;

import com.togezzer.chatsauvegarde.enums.ErrorCode;
import com.togezzer.chatsauvegarde.enums.ErrorResponseField;
import com.togezzer.chatsauvegarde.exception.MessageUuidNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation (ConstraintViolationException ex){
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath().toString().substring(v.getPropertyPath().toString().lastIndexOf('.') + 1)
                        + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        return buildErrorResponse(ErrorCode.VALIDATION_FAILED,details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch (MethodArgumentTypeMismatchException ex){
        return buildErrorResponse(ErrorCode.INVALID_FORMAT,String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseError(DataAccessException ex) {
        log.error("MongoDB error: ", ex);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,"A technical error occurred. Please try again later.");
    }

    @ExceptionHandler(MessageUuidNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMessageUuidNotFound(MessageUuidNotFoundException ex){
        return buildErrorResponse(ErrorCode.MESSAGE_NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred : ", ex);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,"An unexpected error occurred");
    }


    private ResponseEntity<Map<String, Object>> buildErrorResponse(ErrorCode errorCode, String detailMessage) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(ErrorResponseField.ERROR.getFieldName(), errorCode.getMessage());
        response.put(ErrorResponseField.MESSAGE.getFieldName(), detailMessage);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
