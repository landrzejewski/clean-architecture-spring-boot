package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.payments.application.CardNotFoundException;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.application.DateTimeProvider;
import pl.fullstackdeveloper.payments.domain.DomainException;
import pl.fullstackdeveloper.payments.domain.Transaction;
import pl.fullstackdeveloper.payments.domain.TransactionId;
import pl.fullstackdeveloper.payments.domain.TransactionType;

/*
 UWAGA (krok 10 - wersja naiwna): zamówienia "same" obciążają kartę, sięgając do wnętrza płatności:
 portu CardRepository i agregatu Card. Omijają use case AddTransaction, więc:
 - nie działa przeliczanie walut i prowizja,
 - nie są publikowane zdarzenia TransactionAdded,
 - nie działa @Retry przy konflikcie wersji,
 - każda zmiana wnętrza płatności może zepsuć zamówienia.
 */
@Atomic
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final CardRepository cardRepository;
    private final DateTimeProvider dateTimeProvider;

    public PlaceOrderService(final OrderRepository orderRepository, final CardRepository cardRepository,
                             final DateTimeProvider dateTimeProvider) {
        this.orderRepository = orderRepository;
        this.cardRepository = cardRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public OrderId handle(final PlaceOrderCommand command) {
        var order = new Order(new OrderId(), command.description(), command.cardNumber(), command.total());
        var card = cardRepository.findByNumber(command.cardNumber()).orElseThrow(CardNotFoundException::new);
        try {
            var transaction = new Transaction(new TransactionId(), dateTimeProvider.getZonedDateTime(), command.total(), TransactionType.PAYMENT);
            card.registerTransaction(transaction);
            cardRepository.save(card);
            order.markPaid(transaction.id().value().toString());
        } catch (DomainException exception) {
            order.reject();
        }
        return orderRepository.save(order).getId();
    }

}
