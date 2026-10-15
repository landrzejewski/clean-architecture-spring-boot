package pl.fullstackdeveloper.payments.adapters.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.payments.api.PaymentResult;

import java.util.Optional;
import java.util.function.Supplier;

interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
}

@Component
class ProcessedMessages {

    private final ProcessedMessageRepository repository;

    ProcessedMessages(final ProcessedMessageRepository repository) {
        this.repository = repository;
    }

    // Wykonuje operację tylko raz dla danego klucza; przy duplikacie zwraca zapamiętany wynik
    PaymentResult processOnce(final String messageKey, final Supplier<PaymentResult> operation) {
        return find(messageKey).orElseGet(() -> {
            var result = operation.get();
            repository.save(switch (result) {
                case PaymentResult.Completed completed -> new ProcessedMessage(messageKey, true, completed.paymentId(), null);
                case PaymentResult.Rejected rejected -> new ProcessedMessage(messageKey, false, null, rejected.reason());
            });
            return result;
        });
    }

    private Optional<PaymentResult> find(final String messageKey) {
        return repository.findById(messageKey).map(message -> message.isCompleted()
                ? new PaymentResult.Completed(message.getPaymentId())
                : new PaymentResult.Rejected(message.getReason()));
    }

}
