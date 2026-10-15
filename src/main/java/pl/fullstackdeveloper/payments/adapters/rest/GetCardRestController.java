package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.common.cqrs.Bus;
import pl.fullstackdeveloper.payments.queries.GetCardSummaryQuery;
import pl.fullstackdeveloper.payments.queries.GetCardTransactionsQuery;
import pl.fullstackdeveloper.payments.queries.TransactionSummary;

import java.util.List;

// Strona odczytu: kontroler zadaje pytania przez szynę - nie zna ani handlera, ani tabel
@RestController
class GetCardRestController {

    private final Bus bus;
    private final CardRestMapper mapper;

    GetCardRestController(final Bus bus, final CardRestMapper mapper) {
        this.bus = bus;
        this.mapper = mapper;
    }

    @GetMapping("api/cards/{number:\\d{16,19}}")
    ResponseEntity<CardResponse> getCard(@PathVariable final String number) {
        return ResponseEntity.of(bus.ask(new GetCardSummaryQuery(number)).map(mapper::toResponse));
    }

    @GetMapping("api/cards/{number:\\d{16,19}}/transactions")
    List<TransactionSummary> getTransactions(@PathVariable final String number) {
        return bus.ask(new GetCardTransactionsQuery(number));
    }

}
