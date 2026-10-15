package pl.fullstackdeveloper.compare;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.compare.onion.application.TopUpService;
import pl.fullstackdeveloper.compare.onion.domain.model.Card;
import pl.fullstackdeveloper.compare.onion.infrastructure.persistence.InMemoryCardRepository;
import pl.fullstackdeveloper.compare.onion.infrastructure.web.CardController;

import java.math.BigDecimal;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static org.assertj.core.api.Assertions.assertThat;

class OnionArchitectureTest {

    private static final String BASE = "pl.fullstackdeveloper.compare.onion";

    @Test
    void dependencies_point_to_the_core() {
        onionArchitecture()
                .domainModels(BASE + ".domain.model..")
                .domainServices(BASE + ".domain.services..")
                .applicationServices(BASE + ".application..")
                .adapter("persistence", BASE + ".infrastructure.persistence..")
                .adapter("web", BASE + ".infrastructure.web..")
                .check(new ClassFileImporter().importPackages(BASE));
    }

    @Test
    void card_is_topped_up() {
        var repository = new InMemoryCardRepository();
        repository.save(new Card("1111222233334444", BigDecimal.ZERO));
        var controller = new CardController(new TopUpService(repository));

        assertThat(controller.topUp("1111222233334444", "100")).isEqualTo("Balance: 100");
    }

}
