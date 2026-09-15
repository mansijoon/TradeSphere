package com.marketforge.trading.service;

import com.marketforge.trading.domain.Instrument;
import com.marketforge.trading.exception.InstrumentNotFoundException;
import com.marketforge.trading.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentRepository repository;

    public InstrumentService(InstrumentRepository repository) {
        this.repository = repository;
    }

    public List<Instrument> getAll() {
        return repository.findAll();
    }

    public Instrument getBySymbol(String symbol) {
        return repository.findBySymbol(symbol)
                .orElseThrow(() -> new InstrumentNotFoundException(symbol));
    }
}
