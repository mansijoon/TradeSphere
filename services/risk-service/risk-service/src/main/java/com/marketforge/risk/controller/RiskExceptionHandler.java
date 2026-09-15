package com.marketforge.risk.controller;

import com.marketforge.risk.exception.RiskRejectedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RiskExceptionHandler {

    @ExceptionHandler(RiskRejectedException.class)
    public ResponseEntity<Map<String, String>> handleRiskRejection(
            RiskRejectedException exception
    ) {
        return ResponseEntity.unprocessableEntity().body(
                Map.of(
                        "error", "RISK_REJECTED",
                        "message", exception.getMessage()
                )
        );
    }
}
