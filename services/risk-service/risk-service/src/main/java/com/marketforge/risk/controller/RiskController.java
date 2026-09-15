package com.marketforge.risk.controller;

import com.marketforge.risk.dto.RiskValidationRequest;
import com.marketforge.risk.dto.RiskValidationResponse;
import com.marketforge.risk.service.RiskValidationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final RiskValidationService riskValidationService;

    public RiskController(RiskValidationService riskValidationService) {
        this.riskValidationService = riskValidationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<RiskValidationResponse> validate(
            @Valid @RequestBody RiskValidationRequest request
    ) {
        riskValidationService.validate(request);
        return ResponseEntity.ok(
                new RiskValidationResponse(true)
        );
    }
}
