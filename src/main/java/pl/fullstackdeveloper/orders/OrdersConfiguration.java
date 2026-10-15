package pl.fullstackdeveloper.orders;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.fullstackdeveloper.orders.application.*;

@Configuration
public class OrdersConfiguration {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(final OrderRepository orderRepository, final OrderEvents orderEvents) {
        return new PlaceOrderService(orderRepository, orderEvents);
    }

    @Bean
    public OrderProgressService orderProgressService(final OrderRepository orderRepository) {
        return new OrderProgressService(orderRepository);
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
