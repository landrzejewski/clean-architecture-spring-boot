package pl.fullstackdeveloper.payments.queries;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBContainer;

// Wspólna konfiguracja testów strony odczytu: MongoDB w kontenerze (wymaga Dockera)
@TestConfiguration(proxyBeanMethods = false)
public class MongoReadStore {

    @Bean
    @ServiceConnection
    MongoDBContainer mongo() {
        return new MongoDBContainer("mongo:8.0");
    }

}
