package pl.fullstackdeveloper.payments.adapters.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/*
 Ten sam kontrakt portu, ale na prawdziwym PostgreSQL w kontenerze (Testcontainers).
 H2 nie zachowuje się identycznie jak produkcyjna baza (typy, blokady, SQL) - testy adapterów
 warto uruchamiać na tej samej technologii co produkcja. Bez Dockera test jest pomijany.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgresCardRepositoryContractTest extends CardRepositoryContractTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

}
