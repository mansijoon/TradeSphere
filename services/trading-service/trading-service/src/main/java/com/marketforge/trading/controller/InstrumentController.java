package com.marketforge.trading.controller;

import com.marketforge.trading.domain.Instrument;
import com.marketforge.trading.service.InstrumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instruments")
public class InstrumentController {

    private final InstrumentService service;

    public InstrumentController(InstrumentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Instrument> getAll() {
        return service.getAll();
    }

    @GetMapping("/{symbol}")
    public Instrument getBySymbol(@PathVariable String symbol) {
        return service.getBySymbol(symbol);
    }
}
