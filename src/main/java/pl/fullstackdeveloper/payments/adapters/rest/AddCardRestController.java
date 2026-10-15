package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.common.cqrs.Bus;

import static pl.fullstackdeveloper.common.web.LocationUri.fromRequest;

/*
 Adapter wejściowy: tłumaczy HTTP/JSON na komendę, wywołuje port wejściowy i tłumaczy wynik z powrotem.
 Zero logiki biznesowej.
 */
@RestController
class AddCardRestController {

    private final Bus bus;
    private final CardRestMapper mapper;

    AddCardRestController(final Bus bus, final CardRestMapper mapper) {
        this.bus = bus;
        this.mapper = mapper;
    }

    @PostMapping("api/cards")
    ResponseEntity<CardNumberResponse> addCard(@Validated @RequestBody final AddCardRequest request) {
        var cardNumber = bus.send(mapper.toCommand(request));
        var locationUri = fromRequest(cardNumber.value());
        return ResponseEntity.created(locationUri).body(new CardNumberResponse(cardNumber.value()));
    }

}
