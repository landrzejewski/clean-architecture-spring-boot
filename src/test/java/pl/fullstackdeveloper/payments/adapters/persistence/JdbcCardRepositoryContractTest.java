package pl.fullstackdeveloper.payments.adapters.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("jdbc")
class JdbcCardRepositoryContractTest extends CardRepositoryContractTest {
}
