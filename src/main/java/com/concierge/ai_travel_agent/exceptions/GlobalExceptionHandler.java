package com.concierge.ai_travel_agent.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // This tells Spring "Hey, watch every controller and catch their mistakes!"
public class GlobalExceptionHandler {

    // This catches generic, unexpected server crashes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "The concierge service encountered an unexpected issue.");
        errorResponse.put("details", ex.getMessage());

        System.err.println("CAUGHT EXCEPTION: " + ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
