package pl.fullstackdeveloper.payments.adapters.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.CardModifiedConcurrentlyException;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.Optional;

/*
 Adapter wyjściowy: implementuje port aplikacji z użyciem Spring Data JPA.
 MANDATORY - transakcję otwiera use case (@Atomic), adapter nigdy nie robi tego sam.
 */
@Adapter
@Profile("!jdbc")
@Transactional(propagation = Propagation.MANDATORY)
class JpaCardRepositoryAdapter implements CardRepository {

    private final SpringDataCardRepository repository;
    private final JpaCardMapper mapper;

    JpaCardRepositoryAdapter(final SpringDataCardRepository repository, final JpaCardMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Card> findByNumber(final CardNumber cardNumber) {
        return repository.findByNumber(cardNumber.value()).map(mapper::toDomain);
    }

    @Override
    public ResultPage<Card> findAll(final PageSpec pageSpec) {
        var page = repository.findAll(PageRequest.of(pageSpec.index(), pageSpec.size(), Sort.by("number")));
        var cards = page.getContent().stream().map(mapper::toDomain).toList();
        return new ResultPage<>(cards, pageSpec, page.getTotalPages());
    }

    @Override
    public Card save(final Card card) {
        var entity = repository.findById(card.getId().value().toString())
                .map(existing -> {
                    mapper.update(existing, card);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(card));
        try {
            // flush - konflikt wersji ujawnia się tutaj, więc możemy go przetłumaczyć na wyjątek portu
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException exception) {
            throw new CardModifiedConcurrentlyException(exception);
        }
    }

}
