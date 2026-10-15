package pl.fullstackdeveloper.compare;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.compare.hexagonal.adapter.in.web.CardController;
import pl.fullstackdeveloper.compare.hexagonal.adapter.out.persistence.CardPersistenceAdapter;
import pl.fullstackdeveloper.compare.hexagonal.application.service.TopUpService;
import pl.fullstackdeveloper.compare.hexagonal.domain.Card;

import java.math.BigDecimal;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class HexagonalArchitectureTest {

    private static final String BASE = "pl.fullstackdeveloper.compare.hexagonal";
    private final JavaClasses classes = new ClassFileImporter().importPackages(BASE);

    @Test
    void application_core_does_not_depend_on_adapters() {
        noClasses().that().resideInAnyPackage(BASE + ".domain..", BASE + ".application..")
                .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter..")
                .check(classes);
    }

    @Test
    void driving_adapters_use_only_input_ports() {
        noClasses().that().resideInAPackage(BASE + ".adapter.in..")
                .should().dependOnClassesThat().resideInAnyPackage(BASE + ".application.service..", BASE + ".adapter.out..")
                .check(classes);
    }

    @Test
    void card_is_topped_up() {
        var persistence = new CardPersistenceAdapter();
        persistence.save(new Card("1111222233334444", BigDecimal.ZERO));
        var controller = new CardController(new TopUpService(persistence, persistence));

        assertThat(controller.topUp("1111222233334444", "100")).isEqualTo("Balance: 100");
    }

}
