package pl.fullstackdeveloper.compare;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.compare.layered.business.CardService;
import pl.fullstackdeveloper.compare.layered.persistence.CardDao;
import pl.fullstackdeveloper.compare.layered.persistence.CardRecord;
import pl.fullstackdeveloper.compare.layered.presentation.CardController;

import java.math.BigDecimal;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static org.assertj.core.api.Assertions.assertThat;

class LayeredArchitectureTest {

    private static final String BASE = "pl.fullstackdeveloper.compare.layered";

    @Test
    void dependencies_go_top_down() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Presentation").definedBy(BASE + ".presentation..")
                .layer("Business").definedBy(BASE + ".business..")
                .layer("Persistence").definedBy(BASE + ".persistence..")
                .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
                .whereLayer("Business").mayOnlyBeAccessedByLayers("Presentation")
                .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Business")
                .check(new ClassFileImporter().importPackages(BASE));
    }

    @Test
    void card_is_topped_up() {
        var dao = new CardDao();
        dao.update(new CardRecord("1111222233334444", BigDecimal.ZERO));
        var controller = new CardController(new CardService(dao));

        assertThat(controller.topUp("1111222233334444", "100")).isEqualTo("Balance: 100");
    }

}
