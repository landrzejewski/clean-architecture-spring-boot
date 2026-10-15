package pl.fullstackdeveloper.orders.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataOrderRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByCardReference(String cardReference);

}
