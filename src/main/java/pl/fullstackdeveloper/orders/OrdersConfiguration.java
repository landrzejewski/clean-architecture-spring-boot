package pl.fullstackdeveloper.orders;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.fullstackdeveloper.orders.application.*;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.application.DateTimeProvider;

@Configuration
public class OrdersConfiguration {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(final OrderRepository orderRepository, final CardRepository cardRepository,
                                               final DateTimeProvider dateTimeProvider) {
        return new PlaceOrderService(orderRepository, cardRepository, dateTimeProvider);
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
