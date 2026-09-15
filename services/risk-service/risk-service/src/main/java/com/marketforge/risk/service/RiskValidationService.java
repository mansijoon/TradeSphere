package com.marketforge.risk.service;

import com.marketforge.risk.domain.RiskAccount;
import com.marketforge.risk.dto.RiskValidationRequest;
import com.marketforge.risk.exception.RiskRejectedException;
import com.marketforge.risk.repository.RiskRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RiskValidationService {

    private final RiskRepository repository;

    public RiskValidationService(RiskRepository repository) {
        this.repository = repository;
    }

    public void validate(RiskValidationRequest request) {
        RiskAccount account = repository.findAccount(request.accountId())
                .orElseThrow(() -> new RiskRejectedException(
                        "Trading account not found: " + request.accountId()
                ));

        if (!"ACTIVE".equals(account.status())) {
            throw new RiskRejectedException(
                    "Trading account is not active"
            );
        }

        switch (request.side()) {
            case "BUY" -> validateBuy(request, account);
            case "SELL" -> validateSell(request);
            default -> throw new RiskRejectedException(
                    "Unsupported order side: " + request.side()
            );
        }
    }

    private void validateBuy(
            RiskValidationRequest request,
            RiskAccount account
    ) {
        if (request.price() == null) {
            return;
        }

        BigDecimal requiredBuyingPower =
                request.price().multiply(request.quantity());

        if (requiredBuyingPower.compareTo(account.availableBalance()) > 0) {
            throw new RiskRejectedException(
                    "Insufficient buying power: required "
                            + requiredBuyingPower
                            + ", available "
                            + account.availableBalance()
            );
        }
    }

    private void validateSell(RiskValidationRequest request) {
        BigDecimal availableQuantity =
                repository.findPositionQuantity(
                        request.accountId(),
                        request.instrumentId()
                ).orElseThrow(() -> new RiskRejectedException(
                        "No position available for instrument: "
                                + request.instrumentId()
                ));

        if (request.quantity().compareTo(availableQuantity) > 0) {
            throw new RiskRejectedException(
                    "Insufficient position: requested "
                            + request.quantity()
                            + ", available "
                            + availableQuantity
            );
        }
    }
}
