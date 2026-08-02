package com.example.commitmate.core.api;

import com.example.commitmate.core.errors.Exception401;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = AppApiController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AppApiExceptionHandler {
    @ExceptionHandler(Exception401.class)
    public ResponseEntity<Map<String, String>> unauthorized(Exception401 exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", safeMessage(exception)));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", safeMessage(exception)));
    }

    private String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "요청을 처리하지 못했습니다." : message;
    }
}
