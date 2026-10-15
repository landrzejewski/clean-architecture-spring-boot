package pl.fullstackdeveloper;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/*
 Reguły granic między kontekstami (payments, orders).
 W kroku 10 są CELOWO łamane - usuń @Disabled i uruchom test, żeby zobaczyć listę naruszeń.
 Naprawiamy je w 11-language-encapsulation.
 */
class ContextBoundariesTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("pl.fullstackdeveloper");

    @Disabled("Krok 10: payments <-> orders tworzą cykl (GetCardOrdersRestController <-> PlaceOrderService)")
    @Test
    void contexts_are_free_of_cycles() {
        slices().matching("pl.fullstackdeveloper.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    @Disabled("Krok 10: orders sięga do payments.domain i payments.application (CardRepository, Card, Transaction)")
    @Test
    void orders_do_not_access_payments_internals() {
        noClasses().that().resideInAPackage("..orders..")
                .should().dependOnClassesThat().resideInAnyPackage("..payments.domain..", "..payments.application..")
                .check(classes);
    }

}
