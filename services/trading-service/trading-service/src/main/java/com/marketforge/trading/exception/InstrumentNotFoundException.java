package com.marketforge.trading.exception;

public class InstrumentNotFoundException extends RuntimeException {

    public InstrumentNotFoundException(String symbol) {
        super("Instrument not found: " + symbol);
    }
}
