package pl.fullstackdeveloper.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.fullstackdeveloper.payments.model.CardEntity;

import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long> {

    Optional<CardEntity> findByNumber(String number);

}
