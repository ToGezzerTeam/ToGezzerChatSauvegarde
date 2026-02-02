package com.togezzer.chat_sauvegarde.errorHandler;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> handlerConstraintViolation(ConstraintViolationException ex){
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "Validation Failed");
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath().toString().substring(v.getPropertyPath().toString().lastIndexOf('.') + 1)
                        + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        response.put("message", details);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handlerTypeMismatch(MethodArgumentTypeMismatchException ex){
        Map<String, String> response = new LinkedHashMap<>();

        response.put("error", "Format invalide");
        response.put("message", String.format("valeur invalide '%s' pour le parametre '%s'",
                ex.getValue(), ex.getName()));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDatabaseError(DataAccessException ex) {
        log.error("Erreur MongoDB : ", ex);
        Map<String, Object> response = new LinkedHashMap<>()
                ;
        response.put("error", "Internal Server Error");
        response.put("message", "Une erreur technique est survenue. Veuillez réessayer plus tard.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
