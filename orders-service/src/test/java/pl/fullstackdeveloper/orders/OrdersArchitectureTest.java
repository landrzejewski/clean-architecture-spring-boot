package pl.fullstackdeveloper.orders;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "pl.fullstackdeveloper.orders", importOptions = ImportOption.DoNotIncludeTests.class)
class OrdersArchitectureTest {

    // Tylko adapter (ACL) zna kontrakt płatności - rdzeń zamówień mówi wyłącznie własnym językiem
    @ArchTest
    static final ArchRule core_does_not_know_payments_api = noClasses().that().resideInAnyPackage("..orders.domain..", "..orders.application..")
            .should().dependOnClassesThat().resideInAPackage("pl.fullstackdeveloper.payments..");

    @ArchTest
    static final ArchRule only_payments_adapter_knows_payments_api = noClasses().that().resideOutsideOfPackage("..orders.adapters.payments..")
            .should().dependOnClassesThat().resideInAPackage("pl.fullstackdeveloper.payments.api..");

}
