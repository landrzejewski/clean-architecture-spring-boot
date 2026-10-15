package pl.fullstackdeveloper.orders;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.fullstackdeveloper.orders.application.*;

@Configuration
public class OrdersConfiguration {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(final OrderRepository orderRepository, final Payments payments) {
        return new PlaceOrderService(orderRepository, payments);
    }

    @Bean
    public CompletePaymentUseCase completePaymentUseCase(final OrderRepository orderRepository) {
        return new CompletePaymentService(orderRepository);
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
