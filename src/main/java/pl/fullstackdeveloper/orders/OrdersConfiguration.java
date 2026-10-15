package pl.fullstackdeveloper.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.fullstackdeveloper.orders.application.*;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class OrdersConfiguration {

    @Bean
    public OrderSagaOrchestrator orderSagaOrchestrator(final OrderRepository orderRepository, final OrderSagaRepository sagaRepository,
                                                       final Payments payments, final Shipping shipping,
                                                       @Value("${orders.saga.step-timeout:30s}") final Duration stepTimeout,
                                                       @Value("${orders.saga.max-attempts:3}") final int maxAttempts) {
        return new OrderSagaOrchestrator(orderRepository, sagaRepository, payments, shipping, Clock.systemUTC(), stepTimeout, maxAttempts);
    }

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(final OrderRepository orderRepository, final OrderSagaOrchestrator orchestrator) {
        return new PlaceOrderService(orderRepository, orchestrator);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(final OrderRepository orderRepository) {
        return new GetOrderService(orderRepository);
    }

    @Bean
    public GetCardOrdersUseCase getCardOrdersUseCase(final OrderRepository orderRepository) {
        return new GetCardOrdersService(orderRepository);
    }

    @Bean
    public GetOrderSagaUseCase getOrderSagaUseCase(final OrderSagaRepository sagaRepository) {
        return new GetOrderSagaService(sagaRepository);
    }

}
