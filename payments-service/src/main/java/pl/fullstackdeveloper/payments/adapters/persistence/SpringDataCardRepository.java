package pl.fullstackdeveloper.payments.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataCardRepository extends JpaRepository<CardEntity, String> {

    Optional<CardEntity> findByNumber(String number);

}
