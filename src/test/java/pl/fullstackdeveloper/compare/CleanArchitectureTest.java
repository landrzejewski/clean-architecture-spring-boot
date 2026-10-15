package pl.fullstackdeveloper.compare;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.compare.clean.entities.Card;
import pl.fullstackdeveloper.compare.clean.interfaceadapters.controllers.TopUpController;
import pl.fullstackdeveloper.compare.clean.interfaceadapters.gateways.InMemoryCardGateway;
import pl.fullstackdeveloper.compare.clean.interfaceadapters.presenters.TopUpPresenter;
import pl.fullstackdeveloper.compare.clean.usecases.TopUpInteractor;

import java.math.BigDecimal;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static org.assertj.core.api.Assertions.assertThat;

class CleanArchitectureTest {

    private static final String BASE = "pl.fullstackdeveloper.compare.clean";

    @Test
    void dependency_rule_points_inwards() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Entities").definedBy(BASE + ".entities..")
                .layer("UseCases").definedBy(BASE + ".usecases..")
                .layer("InterfaceAdapters").definedBy(BASE + ".interfaceadapters..")
                .layer("Frameworks").definedBy(BASE + ".frameworks..")
                .whereLayer("Frameworks").mayNotBeAccessedByAnyLayer()
                .whereLayer("InterfaceAdapters").mayOnlyBeAccessedByLayers("Frameworks")
                .whereLayer("UseCases").mayOnlyBeAccessedByLayers("InterfaceAdapters", "Frameworks")
                .whereLayer("Entities").mayOnlyBeAccessedByLayers("UseCases", "InterfaceAdapters", "Frameworks")
                .check(new ClassFileImporter().importPackages(BASE));
    }

    @Test
    void card_is_topped_up_and_result_is_presented() {
        var gateway = new InMemoryCardGateway();
        gateway.save(new Card("1111222233334444", BigDecimal.ZERO));
        var presenter = new TopUpPresenter();
        var controller = new TopUpController(new TopUpInteractor(gateway, presenter));

        controller.topUp("1111222233334444", "100");

        assertThat(presenter.getViewModel().message()).isEqualTo("Balance: 100");
    }

}
