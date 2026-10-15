package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.payments.api.PaymentRequest;
import pl.fullstackdeveloper.payments.api.PaymentResult;
import pl.fullstackdeveloper.payments.api.PaymentsApi;

/*
 Zamówienia korzystają wyłącznie z publicznego API płatności. Logika płatności (waluty, prowizja,
 zdarzenia, retry) działa, bo przechodzimy przez use case płatności.
 Brak @Atomic: każdy moduł pilnuje spójności we własnej transakcji (granica modułu = granica transakcji).
 */
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final PaymentsApi paymentsApi;

    public PlaceOrderService(final OrderRepository orderRepository, final PaymentsApi paymentsApi) {
        this.orderRepository = orderRepository;
        this.paymentsApi = paymentsApi;
    }

    @Override
    public OrderId handle(final PlaceOrderCommand command) {
        var order = new Order(new OrderId(), command.description(), command.cardReference(), command.total());
        var request = new PaymentRequest(command.cardReference().value(), command.total().amount(), command.total().currency().getCurrencyCode());
        switch (paymentsApi.pay(request)) {
            case PaymentResult.Completed completed -> order.markPaid(completed.paymentId());
            case PaymentResult.Rejected rejected -> order.reject(rejected.reason());
        }
        return orderRepository.save(order).getId();
    }

}
