package com.marketforge.trading;

import com.marketforge.trading.domain.Instrument;
import com.marketforge.trading.exception.InstrumentNotFoundException;
import com.marketforge.trading.repository.InstrumentRepository;
import com.marketforge.trading.service.InstrumentService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstrumentServiceTest {

    @Test
    void getBySymbolReturnsInstrument() {
        InstrumentRepository repository = mock(InstrumentRepository.class);

        Instrument instrument = new Instrument(
                "AAPL",
                "Apple Inc.",
                "EQUITY",
                "ACTIVE"
        );

        when(repository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(instrument));

        InstrumentService service = new InstrumentService(repository);

        assertEquals("AAPL", service.getBySymbol("AAPL").getSymbol());
    }

    @Test
    void getBySymbolThrowsWhenMissing() {
        InstrumentRepository repository = mock(InstrumentRepository.class);

        when(repository.findBySymbol("INVALID"))
                .thenReturn(Optional.empty());

        InstrumentService service = new InstrumentService(repository);

        assertThrows(
                InstrumentNotFoundException.class,
                () -> service.getBySymbol("INVALID")
        );
    }

    @Test
    void getAllReturnsRepositoryResults() {
        InstrumentRepository repository = mock(InstrumentRepository.class);

        Instrument instrument = new Instrument(
                "AAPL",
                "Apple Inc.",
                "EQUITY",
                "ACTIVE"
        );

        when(repository.findAll()).thenReturn(List.of(instrument));

        InstrumentService service = new InstrumentService(repository);

        assertEquals(1, service.getAll().size());
        verify(repository).findAll();
    }
}
