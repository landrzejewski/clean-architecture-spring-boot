package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.List;

public class GetCardOrdersService implements GetCardOrdersUseCase {

    private final OrderRepository orderRepository;

    public GetCardOrdersService(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<OrderView> handle(final CardNumber cardNumber) {
        return orderRepository.findByCardNumber(cardNumber).stream().map(OrderView::from).toList();
    }

}
