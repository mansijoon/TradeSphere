package com.marketforge.trading;

import com.marketforge.trading.domain.Instrument;
import com.marketforge.trading.repository.InstrumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class InstrumentRepositoryTest {

    @Autowired
    private InstrumentRepository repository;

    @Test
    void findBySymbolReturnsInstrument() {
        Instrument instrument = repository.findBySymbol("AAPL").orElseThrow();

        assertEquals("AAPL", instrument.getSymbol());
        assertEquals("Apple Inc.", instrument.getName());
    }
}
