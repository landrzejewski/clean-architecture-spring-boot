package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.cqrs.Bus;
import pl.fullstackdeveloper.payments.queries.GetCardsQuery;

@RestController
class GetCardsRestController {

    private final Bus bus;
    private final CardRestMapper mapper;

    GetCardsRestController(final Bus bus, final CardRestMapper mapper) {
        this.bus = bus;
        this.mapper = mapper;
    }

    @GetMapping("api/cards")
    PageResponse<CardResponse> getCards(@RequestParam(defaultValue = "0") final int pageIndex,
                                        @RequestParam(defaultValue = "10") final int pageSize) {
        return mapper.toResponse(bus.ask(new GetCardsQuery(new PageSpec(pageIndex, pageSize))));
    }

}
