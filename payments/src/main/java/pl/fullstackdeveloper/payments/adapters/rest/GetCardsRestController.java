package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.payments.application.GetCardsUseCase;

@RestController
class GetCardsRestController {

    private final GetCardsUseCase getCardsUseCase;
    private final CardRestMapper mapper;

    GetCardsRestController(final GetCardsUseCase getCardsUseCase, final CardRestMapper mapper) {
        this.getCardsUseCase = getCardsUseCase;
        this.mapper = mapper;
    }

    @GetMapping("api/cards")
    PageResponse<CardResponse> getCards(@RequestParam(defaultValue = "0") final int pageIndex,
                                        @RequestParam(defaultValue = "10") final int pageSize) {
        var resultPage = getCardsUseCase.handle(new PageSpec(pageIndex, pageSize));
        return mapper.toResponse(resultPage);
    }

}
