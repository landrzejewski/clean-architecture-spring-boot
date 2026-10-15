package pl.fullstackdeveloper;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/*
 Reguły granic między kontekstami - od kroku 11 włączone.
 Java nie pozwala ukryć klas z podpakietów (payments.domain musi być public, żeby widziała go payments.application),
 więc granica "tylko payments.api" jest egzekwowana testem, a nie kompilatorem. To zmienimy w 12/13.
 */
@AnalyzeClasses(packages = "pl.fullstackdeveloper", importOptions = ImportOption.DoNotIncludeTests.class)
class ContextBoundariesTest {

    @ArchTest
    static final ArchRule contexts_are_free_of_cycles = slices().matching("pl.fullstackdeveloper.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule orders_use_only_payments_api = noClasses().that().resideInAPackage("..orders..")
            .should().dependOnClassesThat().resideInAnyPackage("..payments.domain..", "..payments.application..", "..payments.adapters..")
            .because("publicznym kontraktem płatności jest wyłącznie pakiet payments.api");

    @ArchTest
    static final ArchRule payments_do_not_know_orders = noClasses().that().resideInAPackage("..payments..")
            .should().dependOnClassesThat().resideInAPackage("..orders..");

}
