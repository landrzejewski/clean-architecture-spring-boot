package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.common.cqrs.Bus;

import static pl.fullstackdeveloper.common.web.LocationUri.fromRequest;

@RestController
class AddTransactionRestController {

    private final Bus bus;
    private final CardRestMapper mapper;

    AddTransactionRestController(final Bus bus, final CardRestMapper mapper) {
        this.bus = bus;
        this.mapper = mapper;
    }

    @PostMapping("api/cards/{number:\\d{16,19}}/transactions")
    ResponseEntity<TransactionIdResponse> addTransaction(@PathVariable final String number,
                                                         @Validated @RequestBody final AddTransactionRequest request) {
        var transactionId = bus.send(mapper.toCommand(number, request));
        var id = transactionId.value().toString();
        return ResponseEntity.created(fromRequest(id)).body(new TransactionIdResponse(id));
    }

}
