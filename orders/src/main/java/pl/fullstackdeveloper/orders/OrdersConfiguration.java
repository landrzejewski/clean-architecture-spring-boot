package pl.fullstackdeveloper.orders;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.fullstackdeveloper.orders.application.*;
import pl.fullstackdeveloper.payments.api.PaymentsApi;

@Configuration
public class OrdersConfiguration {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(final OrderRepository orderRepository, final PaymentsApi paymentsApi) {
        return new PlaceOrderService(orderRepository, paymentsApi);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(final OrderRepository orderRepository) {
        return new GetOrderService(orderRepository);
    }

    @Bean
    public GetCardOrdersUseCase getCardOrdersUseCase(final OrderRepository orderRepository) {
        return new GetCardOrdersService(orderRepository);
    }

}
