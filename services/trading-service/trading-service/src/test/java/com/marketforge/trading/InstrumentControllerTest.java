package com.marketforge.trading;

import com.marketforge.trading.controller.InstrumentController;
import com.marketforge.trading.domain.Instrument;
import com.marketforge.trading.exception.InstrumentNotFoundException;
import com.marketforge.trading.service.InstrumentService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstrumentControllerTest {

    @Test
    void unknownInstrumentThrowsNotFoundException() {
        InstrumentService service = mock(InstrumentService.class);

        when(service.getBySymbol("INVALID"))
                .thenThrow(new InstrumentNotFoundException("INVALID"));

        InstrumentController controller = new InstrumentController(service);

        assertThrows(
                InstrumentNotFoundException.class,
                () -> controller.getBySymbol("INVALID")
        );
    }

    @Test
    void existingInstrumentIsReturned() {
        InstrumentService service = mock(InstrumentService.class);

        Instrument instrument = new Instrument(
                "AAPL",
                "Apple Inc.",
                "EQUITY",
                "ACTIVE"
        );

        when(service.getBySymbol("AAPL"))
                .thenReturn(instrument);

        InstrumentController controller = new InstrumentController(service);

        Instrument result = controller.getBySymbol("AAPL");

        assertEquals("AAPL", result.getSymbol());
        assertEquals("Apple Inc.", result.getName());
    }

    @Test
    void getAllReturnsInstruments() {
        InstrumentService service = mock(InstrumentService.class);

        Instrument instrument = new Instrument(
                "AAPL",
                "Apple Inc.",
                "EQUITY",
                "ACTIVE"
        );

        when(service.getAll()).thenReturn(List.of(instrument));

        InstrumentController controller = new InstrumentController(service);

        assertEquals(1, controller.getAll().size());
        assertEquals("AAPL", controller.getAll().getFirst().getSymbol());
    }
}
