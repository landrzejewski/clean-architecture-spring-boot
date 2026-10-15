package pl.fullstackdeveloper.orders.adapters.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.fullstackdeveloper.orders.application.*;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static pl.fullstackdeveloper.common.web.LocationUri.fromRequest;

@RestController
@RequestMapping("api/orders")
class OrdersRestController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final GetCardOrdersUseCase getCardOrdersUseCase;

    OrdersRestController(final PlaceOrderUseCase placeOrderUseCase, final GetOrderUseCase getOrderUseCase,
                         final GetCardOrdersUseCase getCardOrdersUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.getCardOrdersUseCase = getCardOrdersUseCase;
    }

    @PostMapping
    ResponseEntity<OrderView> placeOrder(@Validated @RequestBody final PlaceOrderRequest request) {
        var total = new Price(request.amount(), Currency.getInstance(request.currencyCode()));
        var orderId = placeOrderUseCase.handle(new PlaceOrderCommand(request.description(), new CardReference(request.cardNumber()), total));
        var view = getOrderUseCase.handle(orderId);
        // 202 Accepted: zamówienie przyjęte, wynik płatności może przyjść później (status AWAITING_PAYMENT)
        return ResponseEntity.accepted().location(fromRequest(view.id())).body(view);
    }

    @GetMapping("{id}")
    OrderView getOrder(@PathVariable final UUID id) {
        return getOrderUseCase.handle(new OrderId(id));
    }

    // przeniesione z payments (GetCardOrdersRestController) - to zamówienia wiedzą, czym zostały opłacone
    @GetMapping(params = "cardNumber")
    List<OrderView> getCardOrders(@RequestParam final String cardNumber) {
        return getCardOrdersUseCase.handle(new CardReference(cardNumber));
    }

    record PlaceOrderRequest(@NotBlank String description,
                             @NotNull @Pattern(regexp = "\\d{16,19}") String cardNumber,
                             @NotNull @Positive BigDecimal amount,
                             @NotNull @Pattern(regexp = "[A-Z]{3}") String currencyCode) {
    }

}
