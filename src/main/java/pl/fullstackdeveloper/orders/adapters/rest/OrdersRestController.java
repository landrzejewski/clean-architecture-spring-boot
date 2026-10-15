package pl.fullstackdeveloper.orders.adapters.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.fullstackdeveloper.orders.application.GetOrderUseCase;
import pl.fullstackdeveloper.orders.application.OrderView;
import pl.fullstackdeveloper.orders.application.PlaceOrderCommand;
import pl.fullstackdeveloper.orders.application.PlaceOrderUseCase;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static pl.fullstackdeveloper.common.web.LocationUri.fromRequest;

@RestController
@RequestMapping("api/orders")
class OrdersRestController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    OrdersRestController(final PlaceOrderUseCase placeOrderUseCase, final GetOrderUseCase getOrderUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    @PostMapping
    ResponseEntity<OrderView> placeOrder(@Validated @RequestBody final PlaceOrderRequest request) {
        var total = new Money(request.amount(), Currency.getInstance(request.currencyCode()));
        var orderId = placeOrderUseCase.handle(new PlaceOrderCommand(request.description(), new CardNumber(request.cardNumber()), total));
        var view = getOrderUseCase.handle(orderId);
        return ResponseEntity.created(fromRequest(view.id())).body(view);
    }

    @GetMapping("{id}")
    OrderView getOrder(@PathVariable final UUID id) {
        return getOrderUseCase.handle(new OrderId(id));
    }

    record PlaceOrderRequest(@NotBlank String description,
                             @NotNull @Pattern(regexp = "\\d{16,19}") String cardNumber,
                             @NotNull @Positive BigDecimal amount,
                             @NotNull @Pattern(regexp = "[A-Z]{3}") String currencyCode) {
    }

}
