package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.CardReference;

import java.util.List;

@Atomic(readOnly = true)
public class GetCardOrdersService implements GetCardOrdersUseCase {

    private final OrderRepository orderRepository;

    public GetCardOrdersService(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<OrderView> handle(final CardReference cardReference) {
        return orderRepository.findByCardReference(cardReference).stream().map(OrderView::from).toList();
    }

}
